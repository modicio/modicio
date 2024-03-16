# Admin Panel

## Use Cases

| Use Cases                                        | Description                                                                                                                          |
|--------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| Visualize Reference Model                        | Visualize currently selected Reference Model using PlantUML                                                                          |
| See Overview of all Active Variants and Versions | Visualization of variants and Versions                                                                                               |
| Visualize Metadata for each Variant/Version      | For Each Fragment Visualize things like:  <br/> - Trace <br/> - Model <br/> - Interface for each Association Relation <br/> - etc... |
| Change Reference Fragment                        | When seeing details of a Fragment provide the option to select it as the new reference Fragment                                      |
| Clone Fragment                                   | When seeing details of a Fragment provide the option the clone the fragment and its variant/version                                  |
| Change Model of Fragment                         | Provide a way of changing a models Framework through text input                                                                      |

## Mockup

### Use Case 1

The Landing Page of the Admin Panel is meant for the Visualization of the Reference Model. 
The Metadata of the associated Fragment is also shown.

![](img/ReferenceModel.png)

### Use Case 2

By using the side Bar to navigate, one can find an overview of all active Variants and their active Versions.
When clicking on the "More Info" Text the next page opens, where the Metadata of the most Recent Fragment of that Version is displayed.

![](img/VersionsAndVariants.png)

### Use Case 3

This Page shows all the Information about a fragment that might be useful, starting with the Metadata, the trace, the 
interfaces for the nodes in the model and the HeaderElements for the instances.
A click of the button shows the Model associated with the fragment.

![](img/FragmentDetails.png)

![](img/Popup.png)

### Use Cases 4-6

The Following Page is accessed via the "Edit Fragment" button on the details page for a Fragment. It provides ways to 
set the fragment as the reference fragment, clone the fragment and make changes to the model via the text field

![](img/EditFragment.png)