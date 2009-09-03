func_tests = Dir['test/functional/**/*_test.rb'] 	
func_tests.each { |f| load f; }