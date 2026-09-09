import { initializeApp } from 'firebase/app';
import { getFirestore } from 'firebase/firestore';

const firebaseConfig = {
  apiKey: 'AIzaSyAfV6qknKl_5R67HuVCDjJv9iE6Px2zZfM',
  authDomain: 'practicas-417c3.firebaseapp.com',
  projectId: 'practicas-417c3',
  storageBucket: 'practicas-417c3.firebasestorage.app',
  messagingSenderId: '268700411451',
  appId: '1:268700411451:web:025415064b2737ffee1e1e'
};

const app = initializeApp(firebaseConfig);

export const db = getFirestore(app);