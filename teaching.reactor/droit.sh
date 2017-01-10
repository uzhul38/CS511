#!/bin/bash


repositoryList=$(ls -d */)

for i in $repositoryList
  do 
     sudo chown -R reglisse $i
  done
	
