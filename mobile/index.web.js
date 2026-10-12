import '@expo/metro-runtime';
import { LoadSkiaWeb } from '@shopify/react-native-skia/lib/module/web';

LoadSkiaWeb({ locateFile: arquivo => '/' + arquivo }).then(() => {
  require('expo-router/entry');
});
