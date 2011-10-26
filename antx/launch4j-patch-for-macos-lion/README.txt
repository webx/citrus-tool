Launch4j-plugin 1.5.0.0 can't run properly because MacOS Lion has removed PowerPC support.

To fix it, please copy the Intel version of 'ld' and 'windres' to the following directory:

$HOME/.m2/repository/org/bluestemsoftware/open/maven/plugin/launch4j-plugin/1.5.0.0/launch4j-plugin-1.5.0.0-workdir-mac/bin/
