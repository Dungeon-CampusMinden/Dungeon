import glob
import json


def get_token(layer_name):
    if layer_name == 'hole':
        return 'H'
    if layer_name == 'pit':
        return 'P'
    if layer_name == 'skip':
        return 'S'
    if layer_name == 'exit':
        return 'E'
    if layer_name == 'floor':
        return 'F'
    if layer_name == 'wall':
        return 'W'
    if layer_name == 'door':
        return 'D'
    return None


def invert_y_index(y_index, max_height):
    """
    We actually invert the index because the dungeon level will be build from the bottom to the top. Meaning that our
    first line must be the bottom line and the last line must be the top line.
    """
    return max_height - y_index - 1


def get_hero_position(hero_tiles, max_height):
    if hero_tiles and type(hero_tiles) is list:
        if len(hero_tiles) > 1:
            print('Warning: More than one hero pos specified. Using first position.')
        return hero_tiles[0]['x'], invert_y_index(hero_tiles[0]['y'], max_height)


def get_custom_positions(custom_tiles, max_height):
    positions = []
    if custom_tiles and type(custom_tiles) is list:
        for pos in custom_tiles:
            positions.append('{},{}'.format(pos['x'], invert_y_index(pos['y'], max_height)))
        return ';'.join(positions)
    return None


for filepath in glob.iglob('src_maps/*.json'):
    with open(filepath, 'r') as file:
        data = json.load(file)
        # Build matrix
        width, height = data['mapWidth'], data['mapHeight']
        map_matrix = [['' for x in range(width)] for y in range(height)]

        custom_positions = None
        hero_pos = None
        for layer in data['layers']:
            if layer['name'] == 'hero':
                hero_pos = get_hero_position(layer['tiles'], height)
                map_matrix[hero_pos[1]][hero_pos[0]] = 'F'
                continue
            if layer['name'] == 'custom':
                custom_positions = get_custom_positions(layer['tiles'], height)
                continue
            token = get_token(layer['name'])
            # Skip current layer
            if not token:
                continue
            for tile in layer['tiles']:
                map_matrix[invert_y_index(tile['y'], height)][tile['x']] = token

        # First line is empty. Fill with the setting of the map, eg. DEFAULT, FIRE, FOREST, etc.
        file_content = ['']
        # Get hero position (line 2)
        if hero_pos:
            file_content.append('{},{}'.format(hero_pos[0], hero_pos[1]))
        else:
            file_content.append('')
        # Get custom positions (line 3)
        if custom_positions:
            file_content.append(custom_positions)
        else:
            file_content.append('')
        # Get map data (line 4+)
        map_lines = []
        for row in map_matrix:
            file_content.append(''.join(row))
        file_string = '\n'.join(file_content)

        file_name_full = filepath.split('\\')[1]
        file_name = file_name_full.split('.')[0]
        # Write new file
        dist_file = open('./dist_maps/' + file_name + '.level', 'w')
        dist_file.write(file_string)
        dist_file.close()

print('Done')
