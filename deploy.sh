
local_dir=./
server_dir=/home/stackops/home/upload-file-server/webapp

server=stackops@61.28.229.63
jar_file=UploadFIleServer-1.0-SNAPSHOT-FINAL.jar

rsync -a $local_dir/target/$jar_file $server:$server_dir/jar/
