import json
import struct

def get_mat_names(file, node_names):
    with open(file, 'rb') as f:
        f.seek(12)
        chunk_len, chunk_type = struct.unpack('<II', f.read(8))
        j = json.loads(f.read(chunk_len))
        
    nodes = {n.get('name'): n for n in j.get('nodes', [])}
    meshes = j.get('meshes', [])
    materials = j.get('materials', [])
    
    mat_names = set()
    for name in node_names:
        if name in nodes and 'mesh' in nodes[name]:
            mesh_idx = nodes[name]['mesh']
            if mesh_idx < len(meshes):
                for p in meshes[mesh_idx].get('primitives', []):
                    if 'material' in p:
                        mat_idx = p['material']
                        if mat_idx < len(materials):
                            mat = materials[mat_idx]
                            mat_names.add(mat.get('name', f'mat_{mat_idx}'))
    return list(mat_names)

print('1.glb paint:', get_mat_names('app/src/main/assets/models/1.glb', ['Object_2']))
print('1.glb rim:', get_mat_names('app/src/main/assets/models/1.glb', ['Object_14']))
print('2.glb paint:', get_mat_names('app/src/main/assets/models/2.glb', ['Object_12']))
print('2.glb rim:', get_mat_names('app/src/main/assets/models/2.glb', ['Object_19']))

c3_paint = ['Ferrari_F12_car:chassis_carpaint_custom01_LOD2_carpaint_0', 'Ferrari_F12_car:chassis_carpaint_custom02_LOD2_carpaint_0', 'Ferrari_F12_car:chassis_carpaint_LOD2_carpaint_0', 'Ferrari_F12_car:detach_bumper_F_5_carpaint_LOD2_carpaint_0']
c3_rim = ['Ferrari_F12_car:wheelFR_rims_LOD2_rims_0', 'Ferrari_F12_car:wheelFL_rims_LOD2_rims_0', 'Ferrari_F12_car:wheelBR_rims_LOD2_rims_0', 'Ferrari_F12_car:wheelBL_rims_LOD2_rims_0']
print('3.glb paint:', get_mat_names('app/src/main/assets/models/3.glb', c3_paint))
print('3.glb rim:', get_mat_names('app/src/main/assets/models/3.glb', c3_rim))

c4_paint = ['f430:LOD_A_BODY_mm_ext_f430:Vehicle_Exterior_mm_ext1_0', 'f430:LOD_A_BOOT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0', 'f430:LOD_A_DOOR_LEFT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0', 'f430:LOD_A_DOOR_RIGHT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0', 'f430:LOD_A_FRONTBUMPER_mm_ext_f430:Vehicle_Exterior_mm_ext1_0', 'f430:LOD_A_HOOD_mm_ext_f430:Vehicle_Exterior_mm_ext1_0', 'f430:LOD_A_MIRROR_LEFT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0', 'f430:LOD_A_MIRROR_RIGHT_mm_ext_f430:Vehicle_Exterior_mm_ext1_0', 'f430:LOD_A_REARBUMPER_mm_ext_f430:Vehicle_Exterior_mm_ext1_0']
c4_rim = ['f430:LOD_A_WHEEL_mm_wheel_f430:Vehicle_Exterior_mm_wheel1_0', 'LOD_A_WHEEL_mm_wheel1_f430:Vehicle_Exterior_mm_wheel1_0', 'LOD_A_WHEEL_mm_wheel2_f430:Vehicle_Exterior_mm_wheel1_0', 'LOD_A_WHEEL_mm_wheel_f430:Vehicle_Exterior_mm_wheel1_0']
print('4.glb paint:', get_mat_names('app/src/main/assets/models/4.glb', c4_paint))
print('4.glb rim:', get_mat_names('app/src/main/assets/models/4.glb', c4_rim))

c5_paint = ['Object_4']
c5_rim = ['Object_15', 'Object_19', 'Object_23', 'Object_27']
print('5.glb paint:', get_mat_names('app/src/main/assets/models/5.glb', c5_paint))
print('5.glb rim:', get_mat_names('app/src/main/assets/models/5.glb', c5_rim))
