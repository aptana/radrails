# This is a modified version of the stdlib TestRunner.
# All output is removed, except a "pass" or "fail" final result.

require 'test/unit/ui/testrunnermediator'
require 'test/unit/ui/testrunnerutilities'

module Test
  module Unit
    module UI
      module Console
        
        # Runs a Test::Unit::TestSuite on the console.
        class AutoTestRunner
          extend TestRunnerUtilities
          
          # Creates a new TestRunner for running the passed
          # suite. If quiet_mode is true, the output while
          # running is limited to progress dots, errors and
          # failures, and the final result. io specifies
          # where runner output should go to; defaults to
          # STDOUT.
          def initialize(suite, output_level=NORMAL, io=STDOUT)
            if (suite.respond_to?(:suite))
              @suite = suite.suite
            else
              @suite = suite
            end
            @output_level = output_level
            @io = io
            @status = "pass"
          end
          
          # Begins the test run.
          def start
            setup_mediator
            attach_to_mediator
            return start_mediator
          end
          
          private
          def setup_mediator
            @mediator = create_mediator(@suite)
            suite_name = @suite.to_s
            if ( @suite.kind_of?(Module) )
              suite_name = @suite.name
            end
          end
          
          def create_mediator(suite)
            return TestRunnerMediator.new(suite)
          end
          
          def attach_to_mediator
            @mediator.add_listener(TestResult::FAULT, &method(:add_fault))
            @mediator.add_listener(TestRunnerMediator::STARTED, &method(:started))
            @mediator.add_listener(TestRunnerMediator::FINISHED, &method(:finished))
            @mediator.add_listener(TestCase::STARTED, &method(:test_started))
            @mediator.add_listener(TestCase::FINISHED, &method(:test_finished))
          end
          
          def start_mediator
            return @mediator.run_suite
          end
          
          def add_fault(fault)
            if(fault.instance_of?(Test::Unit::Failure))
              @status = "fail"
            elsif(fault.instance_of?(Test::Unit::Error))
              @status = "error"
            end
          end
          
          def started(result)
            @result = result
          end
          
          def finished(elapsed_time)
            output(@status)
          end
          
          def test_started(name)
            
          end
          
          def test_finished(name)
            
          end
          
          def nl(level=NORMAL)
            output("", level)
          end
          
          def output(something, level=NORMAL)
            @io.puts(something) if (output?(level))
            @io.flush
          end
          
          def output_single(something, level=NORMAL)
            @io.write(something) if (output?(level))
            @io.flush
          end
          
          def output?(level)
            level <= @output_level
          end
        end
      end
    end
  end
end

if __FILE__ == $0
  Test::Unit::UI::Console::AutoTestRunner.start_command_line_test
end
