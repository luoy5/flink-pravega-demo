cpath=''
for file in $(ls target/dependency); do
	cpath=$cpath:target/dependency/$file
done


java -cp $cpath:target/flink-pravega-demo-0.0.1-SNAPSHOT.jar $@
#io.pravega.example.flink.demo.MyWriter
#io.pravega.example.flink.demo.MyReader
