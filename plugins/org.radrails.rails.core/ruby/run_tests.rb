unit_tests = Dir['test/unit/**/*_test.rb']
unit_tests.each { |f| load f; }

func_tests = Dir['test/functional/**/*_test.rb'] 	
func_tests.each { |f| load f; }

integration_tests = Dir['test/integration/**/*_test.rb'] 	
integration_tests.each { |f| load f; }