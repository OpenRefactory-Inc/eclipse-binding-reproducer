'''
/*************************************************************************
 *
 * OPENREFACTORY CONFIDENTIAL
 * __________________
 *
 * Copyright (c) 2019 OpenRefactory, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of OpenRefactory, Inc. The
 * intellectual and technical concepts contained herein are proprietary to OpenRefactory, Inc. and
 * may be covered by U.S. and Foreign Patents, patents in process, and are protected by trade secret
 * or copyright law. Dissemination of this information or reproduction of this material is strictly
 * forbidden unless prior written permission is obtained from OpenRefactory, Inc.
 *
 * Contributors: Sakib Fuad (OpenRefactory, Inc.) - Initial API and implementation
'''

#!/usr/bin/python

import os
import xml.etree.ElementTree as ET
from xml.etree.ElementTree import Element, SubElement, Comment, tostring
import xml.dom.minidom

src_file_list = []
lib_file_list = []
jar_file_list = []

#adds the src folders 
def addSrc(filepath, filepath_append):
	if os.path.isdir(filepath):
		subdir_list = os.listdir(filepath)
		for subdir in subdir_list:
			subdir_path = os.path.join(filepath, subdir)
			subdir_path_append = os.path.join(filepath_append, subdir)
			addSrc(subdir_path, subdir_path_append)
	
	elif (os.path.isfile(filepath) and filepath.endswith(".java")):
		with open(filepath, "r") as filereader:
			lines = filereader.readlines()
		
		for line in lines:
			line = line.strip()
			if (line.find("package") == 0):
				lineparts = line.split()
				
				i = 0
				while (i < len(lineparts)):
					if (lineparts[i] != "package" and lineparts[i] != "" and lineparts[i] !="//"):
						break
					i = i+1

				lineparts2 = lineparts[i].split(";")
				lineparts3 = lineparts2[0]
				packagename_list = lineparts3.split(".")
				
				cur_filepath = filepath_append
				package_part_result = ""
				for	package_part in packagename_list:
					package_part_result = package_part_result + package_part + "/"

				cur_filepath = cur_filepath.replace(package_part_result, "")
				
				cur_filepath_parts = cur_filepath.split("/")
				resulted_filepath = ""
				for filepath_part in cur_filepath_parts:
					if(filepath_part.endswith(".java")):
						break
					else:
						resulted_filepath = resulted_filepath + filepath_part + "/"
				
				src_file_list.append(resulted_filepath)
				break


def includeJars(filepath):
	for root, dirs, files in os.walk(filepath, topdown=True):
		for file_name in files:
			if (file_name.endswith(".jar")):
				file_path = os.path.join(root, file_name)
				jar_file_list.append(file_path)
				

def find_java(file_path):
	for files in os.walk(file_path, topdown=True):
		for file_name in files:
			if (str(file_name).endswith(".java")):
				return 1
	return 0


def find_classpaths(source_path):
	subdir_list = os.listdir(source_path)

	for subdir in subdir_list:
		subdir_path = os.path.join(source_path, subdir)
		subdir_path_append = subdir
		addSrc(subdir_path, subdir_path_append)
		
	includeJars(source_path)


def write_file(source_path):
	topcontent = ET.Element("classpath")
	
	src_file_set = set(src_file_list)
	for src_file_name in src_file_set:
		item1 = ET.SubElement(topcontent, "classpathentry", kind="src", path=src_file_name)
	
	for jar_file_name in jar_file_list:
		ET.SubElement(topcontent, "classpathentry", kind="lib", path=jar_file_name)
	
	ET.SubElement(topcontent, "classpathentry", kind="con", path="org.eclipse.jdt.launching.JRE_CONTAINER")

	#create a new XML file with the results
	mydata = ET.tostring(topcontent)
	xml_p = xml.dom.minidom.parseString(mydata)
	pretty_xml = xml_p.toprettyxml()

	new_file_path = os.path.join(source_path, ".classpath")
	with open(new_file_path, "w+") as myfile:
		myfile.write(pretty_xml)
		

def printAllList():
	for src_file_name in src_file_list:
		print(src_file_name)

	print(lib_file_list)
	print(jar_file_list)
	

if __name__ == '__main__':
	source_path = ""
	config_file_path = os.path.join(os.getcwd(), "config")
	
	with open(config_file_path, 'r') as file_reader:
		config_data = file_reader.readlines()

		for line in config_data:
			index = line.find("SOURCE")
			if index > -1:
				words = line.split()
				source_path = words[1]

	find_classpaths(source_path)
	write_file(source_path)	
	print ("Classpath created!")
	
	
	

	
	
			
				
			
