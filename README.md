# URI_parser

This patch of parser is coded such that an input string is taken and converted to URi format using a Reguar expression based on RFC 2396 definitions. This URI is than broken into segments to get scheme, Authority(furthur to userinfo, host, port), path, fragment, query. All these components are also based on RFC 2396 guidelines. Packages are used for Matcher, Pattern, Serialization and Exceptions.

## Input

Input is a string of URI format string and is to passed in console.

### example : 

Enter URI string : 

https://john.doe@www.example.com:123/forum/questions/?tag=networking&order=newest#top


## Output

### example :

=============================================

==============URI PARAMETERS=================

Scheme : https<br/>
Host : www.example.com<br/>
User : john.doe<br/>
Query : tag=networking&order=newest<br/>
Port : 123<br/>
Fragment : top<br/>
Path : /forum/questions/<br/>

=============================================

