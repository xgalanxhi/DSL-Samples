for c in comp1 comp2
do
for v in 1.0 2.0 3.0 3.1
do
	touch ${c}
	ectool deleteArtifactVersion com.example:${c}:${v}
	ectool publishArtifactVersion --version ${v} --groupId com.example --artifactKey ${c} --includePatterns ${c}.war
done
done
