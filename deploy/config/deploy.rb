role :files, "10.10.1.21", :user => 'hudson'

set :application, "rails" # Which project are we pushing (this controls folder name used from Hudson and s3)
set :branch, "master" # which branch was it built on?
# Actual production bucket. Uncomment once we know these scripts are working as we want
#set :bucket_name, 'download.aptana.com' # Which bucket are we pushing it to?
# Testing bucket
set :bucket_name, 'cap-deploy-bundle-test'

set :containing_folder_name, "#{application}"
set :bucket_path_prefix, "tools/radrails/plugin/install"
set :build_artifact_path, "/var/update-site/update/#{branch}" # Where does it live on the build file server?
set :compressed_filename, "#{application}.tar.gz"

namespace :deploy do
  
  desc "Connects to S3 using our credentials"
  task :connect_s3 do 
    require 'aws/s3'
    require 'yaml'
    AWS::S3::Base.establish_connection!(YAML.load_file('config/s3.yml'))   
  end
  
  namespace :plugin do
    desc "Set up variables for plugin deployment (for standalone studio users)"
    task :init do 
      set :application, "rails"
      set :containing_folder_name, "rails"
      set :compressed_filename, "rails.tar.gz"
    end
    
    # TODO Move all deploy:plugin tasks (except init) to deploy namespace since they're shared by all but standalone deployment
    
    # TODO I think I can just grab the zip file that's already in the artifacts and download that only!
    desc "Compress the contents of the build artifacts to speed up downloading them locally"
    task :compress, :roles => :files do
      sudo "rm -f /var/#{compressed_filename}"
      sudo "tar -pczf /var/#{compressed_filename} -C #{build_artifact_path} #{application}"
    end
    
    desc "Download the compressed build artifacts"
    task :grab, :roles => :files do
      get("/var/#{compressed_filename}", compressed_filename)
      sudo "rm -f /var/#{compressed_filename}"
    end
    
    desc "Uncompress the downloaded build artifacts"
    task :uncompress do
      run_locally("tar -xvzf #{compressed_filename}")
      run_locally("rm -f #{compressed_filename}")
    end
    
    # Used in our "legacy" update scheme where we assume unique version numbers to releases and then do apache redirects
    #  desc "Extract version number and set up so we store under the version name as folder in S3"
    #  task :extract_version, :depends => [:uncompress] do
    #    value = Dir.glob("#{application}/*-*.zip")
    #    set :containing_folder_name, value[0].match(/.+(\d+\.\d+\.\d+\.\d+).+/)[1]
    #    set :bucket_path_prefix, "#{bucket_path_prefix}/#{application}"
    #  end
    
    desc "Change the directory name to what we'll use on S3"
    task :rename do
      FileUtils.mv(application, containing_folder_name) if application != containing_folder_name
    end
    
    desc "Cleans any existing release on s3 before pushing over top"
    task :clean_s3 do
      objects = []
      # Apparently this doesn't return everything, so we have to keep looping until empty
      begin
        objects = AWS::S3::Bucket.objects(bucket_name, :prefix => "#{bucket_path_prefix}/#{containing_folder_name}")
        objects.each do |object|
          puts "Removing old #{object.key}"
          object.delete
        end
      end while !objects.empty?
    end
    
    desc "Push the build artifacts up to S3"
    task :push do 
      files = File.join(containing_folder_name, "**", "*")
      Dir.glob(files).each do |filename|
        next if File.directory?(filename)
        puts "Pushing #{filename}..."
        remote_path = "#{bucket_path_prefix}/" + filename
        AWS::S3::S3Object.store(remote_path, open(filename), bucket_name, :access => :public_read)
      end
    end
    
    desc "Cleans up the extract build artifacts locally"
    task :clean do
      run_locally("rm -rf #{containing_folder_name}")
    end
    
    before "deploy:plugin:clean_s3", "deploy:connect_s3"
    before "deploy:plugin:push", "deploy:connect_s3", "deploy:plugin:clean_s3"
    
    desc "Run all the necessary deploy tasks in the correct order for pushing out the plugin"
    task :default do
      deploy.plugin.init
      deploy.plugin.compress
      deploy.plugin.grab
      deploy.plugin.uncompress
      deploy.plugin.rename
      deploy.plugin.push
      deploy.plugin.clean
    end
  end
  
  namespace :standalone do
    
    desc "Push out the standalone builds and installers"
    task :default do
      puts "Not fully implemented yet!!!"
      deploy.standalone.grab
      deploy.standalone.push
    end
    
    desc "Make the local file structure"
    task :mkdirs do
      run_locally("mkdir standalone")
      run_locally("mkdir standalone/linux")
      run_locally("mkdir standalone/mac")
      run_locally("mkdir standalone/win")
    end
    
    desc "Download the compressed build artifacts"
    task :grab_zips, :roles => :files do
      set :build_artifact_path, "/var/builds/#{branch}/radrails-standalone"
      # TODO Need to unzip one of these and parse out the version number, then we need to append it to each of these zips and the installers
      get("#{build_artifact_path}/radrails.linux.gtk.x86_64.zip", "standalone/linux/Aptana_RadRails_Setup_Linux_x86_64.zip")
      get("#{build_artifact_path}/radrails.linux.gtk.x86.zip", "standalone/linux/Aptana_RadRails_Setup_Linux_x86.zip")
      get("#{build_artifact_path}/radrails.macosx.cocoa.x86.zip", "standalone/mac/Aptana_RadRails_Setup_cocoa.zip")
      get("#{build_artifact_path}/radrails.macosx.carbon.zip", "standalone/mac/Aptana_RadRails_Setup_carbon.zip")
      get("#{build_artifact_path}/radrails.win32.x86.zip", "standalone/win/Aptana_RadRails_Setup_Win.zip")
    end
    
    desc "Download the installers"
    task :grab_installers do
      run_locally("wget http://build.aptana.private/hudson/view/master/job/radrails-win/lastSuccessfulBuild/artifact/radrails/builders/build_win_installer/staging/Aptana_RadRails_Setup.exe")
      run_locally("wget http://build.aptana.private/hudson/view/master/job/radrails-mac-installer/lastSuccessfulBuild/artifact/builders/build_mac_installer/staging/Aptana_RadRails.dmg")  
      run_locally("mv Aptana_RadRails_Setup.exe standalone/win/Aptana_RadRails_Setup.exe")
      run_locally("mv Aptana_RadRails.dmg standalone/mac/Aptana_RadRails_Setup.dmg")
    end
    
    task :grab do
      deploy.standalone.grab_zips
      deploy.standalone.grab_installers
    end
    
    before "deploy:standalone:grab", "deploy:standalone:mkdirs"
    
    desc "Push the build artifacts up to S3"
    task :push do
      set :bucket_path_prefix, "tools/radrails"
      files = File.join("standalone", "**", "*")
      Dir.glob(files).each do |filename|
        next if File.directory?(filename)
        puts "Pushing #{filename}..."
        remote_path = "#{bucket_path_prefix}/" + filename
        AWS::S3::S3Object.store(remote_path, open(filename), bucket_name, :access => :public_read)
      end
    end
    
    before "deploy:standalone:push", "deploy:connect_s3"
    
    desc "clean up local copies of artifacts"
    task :clean do
      run_locally("rm -rf standalone")
    end
  end
  
  # TODO Need to add tasks for pushing RCP!
  namespace :rcp do
    desc "Set up variables for RCP deployment"
    task :init do 
      set :application, "radrails-rcp"
      set :containing_folder_name, "radrails-rcp"
      set :compressed_filename, "radrails-rcp.tar.gz"
    end
    
    task :default do
      deploy.rcp.init
      deploy.plugin.compress
      deploy.plugin.grab
      deploy.plugin.uncompress
      deploy.plugin.rename
      deploy.plugin.push
      deploy.plugin.clean
    end
  end
  
  namespace :bundle do
    
    desc "Set up variables for bundle tasks"
    task :init do
      # FIXME Job set to push to "rails-bundle", Sandip has "radrails-bundle" for update site folder name
      set :application, "rails-bundle"
      set :containing_folder_name, "radrails-bundle"
      set :compressed_filename, "radrails-bundle.tar.gz"
    end
    
    desc "Run all the necessary deploy tasks in the correct order for pushing out the Rails + Studio bundle plugin"
    task :default do
      deploy.bundle.init
      deploy.plugin.compress
      deploy.plugin.grab
      deploy.plugin.uncompress
      deploy.plugin.rename
      deploy.plugin.push
      deploy.plugin.clean
    end
  end
  
  task :default do
    # TODO Prompt user if they're trying to push out the plugin, bundle, rcp or the standalone; and then invoke the right one
    deploy.bundle
    deploy.rcp
    deploy.standalone
  end
end