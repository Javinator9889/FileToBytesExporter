# FileToBytesExporter
*Generate binary files from regular text ones*.

## 1. Introduction - *why this library?*

Nowadays, about **~90%** of the digital files are *binary files*. What does it mean? Well, 
basically a **binary file** is a non-structured file no human readable which has so many 
advantages facing **regular text files**:

<ul>
<li>
   <b>Memory efficient</b>: while <i>plain text files</i> has 128 possible values (pure
   unexpanded ASCII), binary files have <b>up to 256 possible values</b>, so it is twice compact (as
   for representing some chars in ASCII, more than one value is needed).
</li>
<li>
   <b>Compact</b>: storing a <i>binary file</i> will necessarily be more packed than a
   <i>plain text file</i>.
</li>
<li>
   <b>No syntax definition</b>: as some human-readable files such as <i>XML, JAVA, etc.
</i> need a <b>specific syntax</b> for saving data correctly, they increase its size and
   complexity with that rules. As opposed to that, <b>binary files</b> just saves the data, with no
   modifications or rules, which is faster and memory friendly.
</li>
<li>
   <b>Directly to memory</b>: as there is <b>no need of processing the data</b>, reading
   a binary file is as simple as it goes directly to the memory for using it.
</li>
<li>
   <b>Secure</b>: as <b>binary file</b> is not human-readable file, it makes impossible
   to others to inspect and know the <i>file contents</i> without a proper editor. Also they need to
   know <b>which type of data structure</b> is saved for obtaining the desired instance.
</li>
</ul>

Definitely, this library allows you to **convert any text file** to binary one, as fast as 
possible using multiple threads for achieving that purpose. In addition, you will be able to 
**merge multiple files** into *one binary file*, by declaring multiple paths where your file is 
in there, or using **glob syntax** for the same purpose.

If you need more information, please read the 
[official documentation](https://javinator9889.github.io/FileToBytesExporter/).

## 2. Installation

For using this library at **any Java application you are using**, you can just *[download from 
"Releases"](https://github.com/Javinator9889/FileToBytesExporter/releases)* or use one of the 
following methods:

### Maven
*First add JCenter to your app*:
```xml
<repositories>
    <repository>
      <id>jcenter</id>
      <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
```

Then, you can just include the lib:
```xml
<dependency>
  <groupId>com.github.javinator9889</groupId>
  <artifactId>filetobytesexporter</artifactId>
  <version>1.0.2</version>
  <type>pom</type>
</dependency>
```

### Gradle
*First, add JCenter to your app*:
```groovy
repositories {
    jcenter()
    // Other repositories you have
}
```

Then, you can just include the lib:
```groovy
implementation 'com.github.javinator9889:filetobytesexporter:1.0.2'
```

### Ivy
*First, add JCenter to your Ivy settings*:
```xml
<ivysettings>
    <resolvers>
        <ibiblio name="bintray"
                 m2compatible="true"
                 root="http://dl.bintray.com/content/example/external-deps"/>
    </resolvers>
</ivysettings>
```

Then, you can just include the lib:
```xml
<dependency org='com.github.javinator9889' name='filetobytesexporter' rev='1.0.2'>
  <artifact name='filetobytesexporter' ext='pom' ></artifact>
</dependency>
```

You must have to **include JCenter()** in order to make it work.

## 3. Contributing

If you find any error or you want to **add a new feature**, you can perfectly:
1. Open a **[new issue](https://github.com/Javinator9889/FileToBytesExporter/issues)** completing
 the *issue template* so it will be easier to solve it.
 
2. Create a new **[pull request](https://github.com/Javinator9889/FileToBytesExporter/pulls)** 
with the changes you have made to the project, and waiting my approval for merging them.
## 4. License
 
     Copyright © 2018 - present | Javinator9889
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see https://www.gnu.org/licenses/.
     
