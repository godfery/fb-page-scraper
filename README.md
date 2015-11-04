# Facebook Page Scraper
Facebook Page Scraper is a tool for collecting data from public facebook pages. This tool uses Facebook's Graph API to collect data. Using this tool, you can collect posts, comments, likes, shares data from public facebook pages. The collected data is written to a MySQL database. Optionally, the tool allows you to download the data in json format.

This tool is especially built for keeping it running and collecting large amount of historical, current and future data (posts, comments etc.) from multiple public facebook pages. Check config.properties.template file for various configuration options for running the tool.      

## Quick start guide
* Install MySQL server
* Create facebook database  
     <pre>CREATE DATABASE facebook 
DEFAULT CHARACTER SET utf8 
DEFAULT COLLATE utf8_general_ci;</pre>
* Download db.schema.sql, config.properties.template and fb-scraper.jar from [latest release] (https://github.com/yhegde/fb-page-scraper/releases/) 
* Create tables in facebook database by running
     <pre>mysql -u root -pRootPwd facebook < db.schema.sql</pre>
* Rename config.properties.template to config.properties, open in a text editor and make relevant changes by following inline instructions
* Start fetching data  
    <pre>java -jar fb-scraper.jar 2>&1 >> log.txt &</pre>

## License  
Copyright [2015] [Yatish Hegde]

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this software except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
