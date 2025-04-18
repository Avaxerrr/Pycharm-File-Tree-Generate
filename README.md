# File Tree Generator

Automatically generates and maintains directory structure documentation for Python projects.

<table>
  <tr>
    <td><img src="https://github.com/user-attachments/assets/06413106-6481-48a3-bec3-3ecfa6846198" width="100%"/></td>
    <td><img src="https://github.com/user-attachments/assets/97a0914d-b5ae-4e03-a230-afb9f7292640" width="100%"/></td>
  </tr>
</table>

![image](https://github.com/user-attachments/assets/31f9ed73-faba-44ec-b778-46c852516d59)


## Overview

File Tree Generator helps developers maintain up-to-date documentation of their project structure by automatically generating file tree diagrams in text or markdown format. The plugin integrates seamlessly with PyCharm and can update documentation in real-time as your project evolves.

**Instant Documentation** - Generate text or markdown documentation of your project structure with one click
* **Live Updates** - Automatically refreshes documentation when files or directories change
* **Highly Customizable** - Configure file patterns, depth levels, and formatting options
* **Python-Aware** - Special highlighting for Python modules and packages
* **Integration** - Accessible directly from PyCharm's Tools menu

## Example Output

```
📁 my_project/
├── 📁 core/
│   ├── 📄 __init__.py
│   ├── 📄 models.py
│   └── 📄 utils.py
├── 📁 tests/
│   ├── 📄 __init__.py
│   └── 📄 test_models.py
├── 📄 README.md
└── 📄 setup.py
```

## Installation

### From JetBrains Marketplace

1. In PyCharm, go to Settings → Plugins → Marketplace
2. Search for "File Tree Generator"
3. Click Install
4. Restart PyCharm when prompted

### Manual Installation

1. Download the latest release `.zip` file from the [Releases page](https://github.com/Avaxerrr/Pycharm-File-Tree-Generator/releases)
2. In PyCharm, go to Settings → Plugins
3. Click the gear icon and select "Install Plugin from Disk..."
4. Navigate to the downloaded `.zip` file
5. Restart PyCharm when prompted

## Usage

### Manual Generation

1. Open your Python project in PyCharm
2. Go to Tools → File Tree Generator
3. Configure your preferences in the dialog that appears
4. Click Generate

### Automatic Updates

1. Go to Settings → Tools → File Tree Generator
2. Enable "Auto-update on file changes"
3. Configure additional settings as needed
4. Click Apply

## Configuration Options

* **Output Format**: Choose between plain text (.txt) or Markdown (.md)
* **Output Location**: Specify where to save the generated documentation
* **Include Patterns**: Define which files/directories to include (supports glob patterns)
* **Exclude Patterns**: Define which files/directories to exclude
* **Depth Level**: Control how deep the directory tree should be traversed
* **Show File Sizes**: Toggle display of file sizes
* **Group by Type**: Organize files by their type

## Building from Source

This project uses Gradle for building:

```bash
# Clone the repository
git clone https://github.com/Avaxerrr/Pycharm-File-Tree-Generator.git
cd Pycharm-File-Tree-Generate

# Build with Gradle
./gradlew build

# The plugin ZIP file will be in build/distributions/
```

## Support

For bug reports and feature requests, please [create an issue](https://github.com/Avaxerrr/Pycharm-File-Tree-Generator/issues) on the GitHub repository.

For questions and discussions, visit the [Discussions](https://github.com/Avaxerrr/Pycharm-File-Tree-Generator/discussions) page.

## License

This plugin is available under the MIT License. See the [LICENSE](license.md) file for more information.
