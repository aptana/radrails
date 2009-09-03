require 'config/boot'
require 'rake'

rake = Rake::Application.new
rakefile_loc = "Rakefile"
rake.do_option('--rakefile',rakefile_loc)
rake.do_option('--rakelibdir','../')
rake.load_rakefile

Rake::Task[ARGV[0]].invoke