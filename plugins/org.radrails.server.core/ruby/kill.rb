signal = ARGV[0]
pid = ARGV[1].to_i
if signal && pid
Process.kill(signal,pid)
end