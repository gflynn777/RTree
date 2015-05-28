#RTree
This project is a multidimensional indexing and querying RTree program. The implementation stores given data points on disk pages and retrieves them as needed during query processing. To increase the size of the tree, an attribute field is included in each tuple of size 500 char.

#Compiling Instructions
The RTree program compiles very simply on a linux machine using the included makefile by simply typing “make” inside the RTree root directory. The makefile ensures that the files are compiled in the correct order (1. Rectangle.java 2. Tuple.java 3. Node.java 4. RTree.java). Once the files are compiled, the program can be run in the following format: java –cp bin main.RTree 



#Usage Instructions
<list>
<li>
Once initiated, the RTree program will save all index pages to disk and create an in-memory R-Tree structure. It then enters a loop asking the user for input while giving 5 different options:
</li>
<li>
M : This takes an integer (ex. M 8) and recreates the tree structure using the given integer as the new maximum fill for each node.
</li>
<li>
P: Prints the resulting tuples of a previously done query. Printing the query results will print the x, y coordinates of each point along with the 500th index of the tuple description to show that the descriptions are all full. 
</li>
<li>
N: Starts a new query. For a single point query, the user can enter the same numbers for both prompts. The program then displays how many results are within the given query and how many pages were accessed (index pages and leaf pages). 
</li>
<li>
F: Finds the points that maximize a function of two given inputs per the extra credit assignment.
</li>
<li>
Q: Ends the program.
</li>
</list>


#Design Decisions
RTree was designed so that all of the leaf nodes in the tree are saved as individual files in the temp directory. Given a page size of 4096 bytes and a leaf node size of 1036, we can fit floor(4096/1036) = 3 tuples into each page. Deciding how many index nodes we can fit into a page was very different. The index nodes have a size of only 36 bytes. Therefore, we can fit floor(4096/36) = 113 index nodes in one page. With a node size this large and the given dataset, every query will have to access every page (since the index structure is only going to be one level). For this reason, I made it so a user can change the max node fill size from within the program to specify a more appropriate size that makes better use of the R-Tree structure.



#Building the Tree
Every point from a dataset file is first read into a list. The list is then sorted according to the Hilbert curve value of each point. The points are put into tuples, and leaf nodes are initialized with 3 tuples per 4096 byte page and put into object files in the temp folder. The leaf nodes are then split into groups of “maxNodeSize” (which is initially calculated to be 113). The process of splitting each lower level into these groups is done recursively until we are left with only one node as the root of the tree.



#Querying
The way the search process works is by first taking in a node and looking at the max bound rectangles of each child node to see if the inputted range query intersects that child’s maximum bound rectangle. If it does, the method recursively enters that node. When the method gets down to the level prior to the leaf nodes, it pulls the leaf pages from disk and enters there matching points into the result list. We were told to simulate disk I/O’s. So, even though the index nodes are not actually on the disk, the program counts each child node access as a page access.



Maximizing function
In order to find the points that maximize the function. The program traverses each level of the tree first looking for the max x and y values of each child node’s mbr. After the highest mbr is found, the program does another pass through the same level to find any node mbrs that intersect the highest mbr. Each of these nodes are then traversed recursively in order to ensure that the highest point is returned.
