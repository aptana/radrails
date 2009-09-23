role :files, "10.10.1.21", :user => 'hudson'

set :application, "rails" # Which project are we pushing (this controls folder name used from Hudson and s3)
set :branch, "trunk" # which branch was it built on?
set :bucket_name, 'update-1.5.x-production' # Which bucket are we pushing it to?
set :build_artifact_path, "/var/update-site/update/#{branch}" # Where does it live on the build file server?
set :compressed_filename, "#{application}.tar.gz"

namespace :deploy do
  # TODO I think I can just grab the zip file that's already in the artifacts and download that only!
  desc "Compress the contents of the build artifacts to speed up downloading them locally"
  task :compress, :roles => :files do
    sudo "tar -pczf /var/#{compressed_filename} -C #{build_artifact_path} #{application}"
  end
  
  desc "Download the compressed build artifacts"
  task :grab, :depends => [:compress], :roles => :files do
    get("/var/#{compressed_filename}", compressed_filename)
    sudo "rm -f /var/#{compressed_filename}"
  end
  
  desc "Uncompress the downloaded build artifacts"
  task :uncompress, :depends => [:grab] do
    run_locally("tar -xvzf #{compressed_filename}")
    run_locally("rm -f #{compressed_filename}")
  end
  
  desc "Extract the release version number and change the containing directory name to it"
  task :rename, :depends => [:uncompress] do
    value = Dir.glob("#{application}/*-*.zip")
    set :release_version, value[0].match(/.+(\d+\.\d+\.\d+\.\d+).+/)[1]
    FileUtils.mv(application, release_version)
  end
  
  desc "Push the build artifacts up to S3"
  task :push, :depends => [:rename] do
    require 'aws/s3'
    # TODO Store the S3 credentials in a YAML config file?
    AWS::S3::Base.establish_connection!(
    :access_key_id     => '0ZA7YV7DBK6JCYAV77G2',
    :secret_access_key => 'MHKoSpej/rtUezG0wfbb12ZIeqayOLFJ/C7n8O/L'
    )    
    files = File.join(release_version, "**", "*")
    Dir.glob(files).each do |filename|
      next if File.directory?(filename)
      # push this sucker to S3!
      puts "Pushing #{filename}..."
      remote_path = "#{application}/" + filename
      AWS::S3::S3Object.store(remote_path, open(filename), bucket_name, :access => :public_read)
    end
  end
  
  desc "Run all the necessary deploy tasks in the correct order"
  task :default do
    deploy.compress
    deploy.grab
    deploy.uncompress
    deploy.rename
    deploy.push
  end
end