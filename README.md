# Overview
JTar is a simple Java Tar library, that provides an easy way to create and read tar files using IO streams. The API is very simple to use and similar to the _java.util.zip_ package and also __supports UStar format__.

[![Build Status](https://travis-ci.org/kamranzafar/jtar.png?branch=master)](https://travis-ci.org/kamranzafar/jtar)

## Usage
JTar is available in maven central and can be added as a dependency in the maven project.
<pre><code>  &lt;dependency&gt;
    &lt;groupId&gt;org.kamranzafar&lt;/groupId&gt;
    &lt;artifactId&gt;jtar&lt;/artifactId&gt;
    &lt;version&gt;2.3&lt;/version&gt;
  &lt;/dependency&gt;
</code></pre>

Below are some examples of using jtar in applications

### Tar example - using TarOutputStream
<pre><code>  // Output file stream
  FileOutputStream dest = new FileOutputStream( "c:/test/test.tar" );
  
  // Create a TarOutputStream
  TarOutputStream out = new TarOutputStream( new BufferedOutputStream( dest ) );
  
  // Files to tar
  File[] filesToTar=new File[2];
  filesToTar[0]=new File("c:/test/myfile1.txt");
  filesToTar[1]=new File("c:/test/myfile2.txt");
  
  for(File f:filesToTar){
     out.putNextEntry(new TarEntry(f, f.getName()));
     BufferedInputStream origin = new BufferedInputStream(new FileInputStream( f ));
     int count;
     byte data[] = new byte[2048];
  
     while((count = origin.read(data)) != -1) {
        out.write(data, 0, count);
     }
  
     out.flush();
     origin.close();
  }
  
  out.close();
</code></pre>

### Untar example - using TarInputStream
<pre><code>  String tarFile = "c:/test/test.tar";
  String destFolder = "c:/test/myfiles";
  
  // Create a TarInputStream
  TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tarFile)));
  TarEntry entry;
  
  while((entry = tis.getNextEntry()) != null) {
     int count;
     byte data[] = new byte[2048];
     FileOutputStream fos = new FileOutputStream(destFolder + "/" + entry.getName());
     BufferedOutputStream dest = new BufferedOutputStream(fos);
  
     while((count = tis.read(data)) != -1) {
        dest.write(data, 0, count);
     }
  
     dest.flush();
     dest.close();
  }
  
  tis.close();
</code></pre>

___Tip: Always use buffered streams with jtar to speed up IO.___

## Examples and resources

* See [JTarTest](https://github.com/kamranzafar/jtar/blob/master/src/test/java/org/kamranzafar/jtar/JTarTest.java) class, provided with the source, for more detailed examples.
* Visit the wiki page for more details on [Tar format](http://en.wikipedia.org/wiki/Tar_%28file_format%29)

__JTar is available in [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.kamranzafar%22%20a%3A%22jtar%22)__
