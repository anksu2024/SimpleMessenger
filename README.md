SimpleMessenger
===============

Simple Chatting Applications to simulate communication between two Android Devices

PROJECT SPECIFICATION: https://docs.google.com/document/d/1KdTkmhfnis2_qpNFDPgx-L7PUWEM7n8HrG62Hdyhs0M/pub

TEST SCRIPTS: The test scripts (Depending on the Operating System) can be downloaded from the links provided in the project specifications.

NOTE: FYR the project specifications and test scripts are also provided in the Respective Folders

11 STEPS TO REALIZE THIS PROJECT:

1) Check the directory "Test Scripts for Project".

2) This directory contains the python scripts that are to be executed to test the project

3) Under this directory in the Terminal execute the script 'create_avd.py' to create 2 AVDs
   Use the command "python create_avd.py 2"

4) Once the AVDs are created, excute the script 'run_avd.py' script to start 2 AVDs
   For the purpose use the command: "python run_avd.py 2"

5) When the 2 AVDs are up and running, each AVD needs to be assigned a port number

6) To do this execute the script 'set_redir.py'
   Command to be used: "python set_redir.py 10000"
   This command will make sure that all the AVDs are connected to each other by a single Server port number 10000

7) Now the tester is good to go ahead with the execution of the App on the AVDs.

8) I used Eclipse to install the SimpleMessenger App on my AVDs.

9) Once the App is up and running on both the AVDs, the tester may play around with it.

10) Type some text in Edit Text given in the bottom of the App in the AVD.

11) Press Enter Key and BAM!! the other AVD receives the text. Same goes with the other AVD.


Please send your comments or let me know if you find a bug to sarrafan[at]buffalo[dot]edu

Thanks.
