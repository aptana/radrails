require 'autotestrunner'
require 'test/unit/collector/dir'

c = Test::Unit::Collector::Dir.new
s = c.collect(*ARGV)
Test::Unit::UI::Console::AutoTestRunner.run(s)