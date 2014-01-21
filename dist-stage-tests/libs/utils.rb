class Logger 

  def self.run(cmd)
    puts "run [#{cmd}]"
    IO.popen(cmd, :err=>[:child, :out] ) do |io|
      while line = io.gets
        cleaned = line.chomp
        puts "#{cleaned}" unless cleaned == nil or cleaned.empty?
      end
        io.close
        raise "An error occured" if $?.to_i != 0
    end
  end
end
