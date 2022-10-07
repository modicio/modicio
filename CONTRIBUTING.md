# Contribution Rules

## Commits

* Keep commits small
* Commit coherent changes together
* **Name the commit after the following schema:**
  * ``[Add|Remove|Refactor|Implement|Test|Fix...] [ClassName|ModuleName|Feature|Issue|...] for/to [small annotation]``
  * Example: ``Refactor Registry.scala``
  * Example: ``Implement datatype classes for improved validation``
* In any case, make sure to start the message with a imperative verb! The issue should read like a command. 
* The description part of the issue can contain explanatons or "closes #issue" references.

## Issues

* Use issues!
* Also use issues for your own TODOs (see Branch section)
* Issues can be created to address:
  * Bugs
  * Requested or planned features
  * Requested or planned improvements
* Add a short but on-point title
* Add sufficient description
* Add tags/labels 
* Assign someone only if responsibilities are clear!
* Comment issues if there is something important to discuss
* Add issue to the "modicio Roadmap" project and move it to the correct column

## Branches

* All branches must be visible on GitHub
* Commit must be made public to the origin branch every few days at least, best daily.
* Branches are only and always created from issues.
* First create an issue, then select "create branch from issue" in the Github UI
  * In consequence, branches are automatically named: ``[issue-id]-issue-title`` 
* If, during development a branch is going to target multiple issues, reference the issues to the original branch issue.

## Merges & Pull Requests

* Do not merge branches into each
* If a branch lives for a long time, the main branch can be pulled or rebased into it. Please do so locally.
* If development on a branch is finished, create a pull request
  * Make sure your code is documented! After the PR is merged to main, the bot will compile the doc and push it to the documentation webpage.
  * Link all issues the branch closes to the PR
  * Give a short description if required
  * Everyone can comment the PR
  * Maintainers can reject the PR
  * The CI (build & test) must succeed before completing a PR 

## Discussions

* Github discussions are enabled for modicio.
* Use discussions if the discussion may be important for future contributors or framework users!
* Mention (tag) others in a new discussion so that they are notified.

## License & Copyright

* modicio is licensed under Apache-2
* If a new (code) file is created, add the standard license comment as first statement on top.
  * You may just copy it from an existing file
  * Enter [your name] OR [your name + (GitHub name)] to the copyright section
  * Make sure to enter the current year
  * If you edit the code in a file by additions, deletions or refactorings, add your name with year as a new line to the copyright section.
    * Small layout changes such as removing white lines or pressing the format button in the IDE do not really create a copyright claim...   
