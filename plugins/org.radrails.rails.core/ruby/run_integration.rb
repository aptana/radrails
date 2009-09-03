func_tests = Dir['test/integration/**/*_test.rb'] 	
func_tests.each { |f| load f; }