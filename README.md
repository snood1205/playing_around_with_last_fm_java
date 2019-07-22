It's [Playing Around With Last.fm](https://github.com/snood1205/playing_around_with_last_fm) with a Java CLI now. 

Instructions
1. To just fetch and have the JSON print out to STDIN run `mvn exec:java -Dexec.mainClass=Runner`.
2. To specify only since a certain time, find that time in seconds since epoch and pass it as an argument. For example
`mvn exec:java -Dexec.mainClass=Runner -Dexec.args="--last-time 1563768000`.
3. To specify printing to another file pass that in as an argument, for example:
`mvn exec:java -Dexec.mainClass=Runner -Dexec.args="--output-file example.json"`.
4. To specify since a certain time and an output file combine the arguments, for example:
`mvn exec:java -Dexec.mainClass=Runner -Dexec.args="--output-file example.json --last-time 1563768000"`.

