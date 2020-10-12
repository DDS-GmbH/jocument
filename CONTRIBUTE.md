# Contribution Guide
If you came to this page, it means that you are interested in 
contributing to jocument.
We are glad to hear that and would like to offer you some guidelines,
rules, and tips for it.

## Goal of the project
At first, make sure your contribution is in line with the goals of the 
jocument project.

jocument aims to be a java-based document generation engine which 
relies on non-standard templates, like DOCX documents or XLSX documents.
Under the hood we aim to have a simple, but extendable architecture,
which is not fixed to one certain implementation of placeholder
resolvers or data sources.

If you are not sure whether your planned contribution fits into 
jocument, just open a `WIP:` pull request, and a maintainer will answer
you as soon as they have time for it.

## Coding standards
Generally jocument is developed with Java 15, please try to use modern
language features wherever it makes sense.
One big part of this is avoiding to use `null` to communicate 
information, if it is necessary to pass back empty objects please use
`Optional` another fitting mechanism.

### Checkstyle
In this project we use checkstyle with a modified Google style to make 
sure the code has a consistent layout.
You can find the `checkstyle.xml` in the config folder, please use
it to format any code you wrote before committing it.


### Logging
There are no real logging guidelines yet, but in general we attempt to
log the beginning and end of important processes with the `info` level,
and relevant more detailed information about function execution with 
the `debug` level.

### Testing
In general, we have three levels of tests:
* On the unit test level we verify the correctness of certain important
or complex functions automatically.
* On the system test level, we have automated tests using 
[poipath](https://github.com/DDS-GmbH/poipath) to verify features
going through multiple levels of jocument.
* On the acceptance test level we have manual tests to check for more
complex cases.

If you contribute a feature to jocument, please try to write unit tests
as appropriate and at least one automated system test demonstrating how 
your feature works.
If you expect the feature to have complex relationships with other
features please also write an integration test with sufficient
documentation so other developers know what outcome of this test to
expect.

Please tag your tests accordingly, at the moment we use `automated`
to mark automated tests and `xwpf`/`xssf` to mark tests for the
corresponding template type.

## Git
Naturally, we use git to do version control and to manage
 source code extensions.

### Branching
When developing, please create a new branch from the `master` branch.
The branch name starts with the class of the branch, followed by a
slash and a one- or two-worded identifier for the branch.
The classes are as follows:

| Prefix     | Description                                                               | Example                                                                |
|------------|---------------------------------------------------------------------------|------------------------------------------------------------------------|
| feature/   | A new feature is being developed.                                         | Parametrizable placeholders                                            |
| fix/       | A bug is fixed.                                                           | `Null Pointer Exception` when resolving the same placeholder two times |
| refactor/  | We change some existing implementation for stability/performance reasons. | Use Enum-based `switch-case` for resolving in the `ReflectionResolver` |
| polishing/ | Collection branch of smaller refactoring.                                 | Switch to `instanceof` pattern matching                                |

### Commits
Please try to group commits so that each commit bundles a set of
joint code changes.
For example, assemble one commit `Add infrastructure` and one 
`Add tests`
The commit message should follow the guidelines described in 
[How to write a commit message](https://chris.beams.io/posts/git-commit/)
by Chris Beams.

## GitHub
We use GitHub to manage the source code, keep track of issues, and
handle pull requests.

### Issues
If your contribution requires some discussion it might be useful
to create an issue first, describing the problem you are facing.
If you already have some path of action in mind you can also open a
 `WIP:` pull request directly.

### Pull requests
When opening a pull request, please give it a meaningful name and
description.
Either link to the corresponding issue or describe the problem in the
pull request if it is of a size not warranting opening an issue.
Furthermore, please add the applicable tags and request a review by
a maintainer (at the moment [@alexpartsch](https://github.com/alexpartsch)
and [@AntonOellerer](https://github.com/AntonOellerer))

### Actions
Currently, we have two github actions in place, of which one is
important for regular contributors, the `check` action.
This action is responsible for checking your code by:
* Running [checkstyle](https://checkstyle.org/)
to make sure the code fits to the style requirements.
* Running an [owasp](https://owasp.org/www-project-dependency-check/)
dependency check to make sure there are no dependencies on 
insecure packages
* Running all automated tests to make sure no bugs were introduced.

This action is triggered by new pushes to the repository and runs on 
the most recent version of the code in the corresponding branch.