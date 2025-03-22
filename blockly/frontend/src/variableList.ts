export const setupVariableDisplay = (addVarCallback: () => void) => {
  const toolboxDiv = document.getElementsByClassName('blocklyToolboxDiv')[0] as HTMLDivElement;
  toolboxDiv.style.display = 'flex';
  toolboxDiv.style.flexDirection = 'column';
  toolboxDiv.style.justifyContent = 'space-between';

  const variableDiv = document.createElement('div');
  variableDiv.id = 'variableDiv';
  variableDiv.className = 'variableDiv';
  toolboxDiv.appendChild(variableDiv);

  const addVariableButton = document.createElement('button');
  addVariableButton.id = 'addVariableButton';
  addVariableButton.innerText = '+';
  addVariableButton.title = 'Variable hinzufügen';
  addVariableButton.onclick = () => {
    addVarCallback();
  };

  const variableText = document.createElement('p');
  variableText.id = 'variableText';
  variableText.innerText = 'Variablen';
  variableDiv.appendChild(variableText);

  const variableList = document.createElement('div');
  variableList.id = 'variableList';
  variableDiv.appendChild(variableList);

  // Add the "no variables" message initially
  showNoVariablesMessage();
  variableDiv.appendChild(addVariableButton);
};

// Check if the variable list is empty
const isVariableListEmpty = (): boolean => {
  const variableList = document.getElementById('variableList') as HTMLDivElement;
  return variableList.getElementsByClassName('variable-item').length === 0;
};

// Show the "no variables" message when the list is empty
const showNoVariablesMessage = () => {
  const variableList = document.getElementById('variableList') as HTMLDivElement;
  // Remove existing message if it exists
  const existingMessage = document.getElementById('no-variables-message');
  if (existingMessage) {
    existingMessage.remove();
  }

  // Check if we need to add the message
  if (isVariableListEmpty()) {
    const noVariablesMessage = document.createElement('p');
    noVariablesMessage.id = 'no-variables-message';
    noVariablesMessage.className = 'no-variables-message';
    noVariablesMessage.innerText = 'Keine Variablen gesetzt';
    variableList.appendChild(noVariablesMessage);
  }
};

export const addVariable = (name: string, value: string, removeVarCallback: (varName: string) => void) => {
  console.log('Adding variable:', name, value);
  const variableList = document.getElementById('variableList') as HTMLDivElement;

  // Remove the "no variables" message if it exists
  const noVariablesMessage = document.getElementById('no-variables-message');
  if (noVariablesMessage) {
    variableList.removeChild(noVariablesMessage);
  }

  const exampleVariable = document.createElement('div');
  exampleVariable.className = 'variable-item';

  const deleteButton = document.createElement('button');
  deleteButton.className = 'delete-variable-btn';
  deleteButton.innerHTML = '−'; // White minus sign
  deleteButton.title = 'Variable löschen';
  deleteButton.onclick = (_) => {
    removeVarCallback(name);
  }
  exampleVariable.appendChild(deleteButton);

  const innerVariableName = document.createElement('p');
  innerVariableName.className = 'variable-name';
  innerVariableName.innerText = name;
  exampleVariable.appendChild(innerVariableName);

  const innerVariableValue = document.createElement('p');
  innerVariableValue.className = 'variable-value';
  innerVariableValue.innerText = value;
  exampleVariable.appendChild(innerVariableValue);

  variableList.appendChild(exampleVariable);

  console.log('Added variable:', name, value);
}

export const updateVariable = (name: string, value: string) => {
  const variableList = document.getElementById('variableList') as HTMLDivElement;
  const variableItems = variableList.getElementsByClassName('variable-item');

  for (let i = 0; i < variableItems.length; i++) {
    const item = variableItems[i];
    const innerVariableName = item.getElementsByClassName('variable-name')[0] as HTMLParagraphElement;
    if (innerVariableName.innerText === name) {
      const innerVariableValue = item.getElementsByClassName('variable-value')[0] as HTMLParagraphElement;
      innerVariableValue.innerText = value;
      break;
    }
  }

  console.log('Updated variable:', name, value);
};

export const removeVariable = (name: string) => {
  const variableList = document.getElementById('variableList') as HTMLDivElement;
  const variableItems = variableList.getElementsByClassName('variable-item');

  for (let i = 0; i < variableItems.length; i++) {
    const item = variableItems[i];
    const innerVariableName = item.getElementsByClassName('variable-name')[0] as HTMLParagraphElement;
    if (innerVariableName.innerText === name) {
      variableList.removeChild(item);
      break;
    }
  }

  console.log('Removed variable:', name);

  // Show "no variables" message if the list is now empty
  showNoVariablesMessage();
};

export const renameVariable = (oldName: string, newName: string) => {
  const variableList = document.getElementById('variableList') as HTMLDivElement;
  const variableItems = variableList.getElementsByClassName('variable-item');

  for (let i = 0; i < variableItems.length; i++) {
    const item = variableItems[i];
    const innerVariableName = item.getElementsByClassName('variable-name')[0] as HTMLParagraphElement;
    if (innerVariableName.innerText === oldName) {
      innerVariableName.innerText = newName;
      break;
    }
  }

  console.log('Renamed variable:', oldName, 'to', newName);
}

