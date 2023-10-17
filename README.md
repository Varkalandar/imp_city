There was a long break since 2018, but recently I felt some motivation to continue this project. So here it is, in a brand new repository and updated to Java 11.

The build process is also new, now using gradle, and making release bundles is, well, totally untested so far. 

Still if I made no mistakes it should suffice to get the release bundle for your operationg system (Windows, Linux), unpack it and run "start.bat" or "start.sh" respectively.

Consider this project to be in some sort of alpha state after the long break and all the changes.

Once I started a manual over there. Outdated, but better than nothing, I guess:
https://gedankenweber.wordpress.com/imp-city-underground-creature-keeping/

Since 2023-10-17 there is now an automated way to create a runnable distribution for Linux from the contents of the repository.

1) Open a terminal, cd into the top level directory of the project
2) Run "./gradlew createDistribution"
3) In the directory app/build/imp_city-vXYZ-linux there should now be a runnable distribution
4) It can be started with the start.sh script 
