import { Text, View, StyleSheet, Pressable, Alert } from 'react-native';
import { NitroAdvertisingIdHybridObject } from 'react-native-nitro-advertising-id';

export default function App() {
  const onPress = async () => {
    const result = await NitroAdvertisingIdHybridObject.requestPermission();

    Alert.alert('result', result);
  };

  const onPress2 = () => {
    const result = NitroAdvertisingIdHybridObject.getAdvertisingId();

    Alert.alert('result', result);
  };

  return (
    <View style={styles.container}>
      <Text>Result</Text>
      <Pressable onPress={onPress}>
        <Text>Trigger Permission</Text>
      </Pressable>

      <Pressable onPress={onPress2}>
        <Text>Trigger ID</Text>
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
