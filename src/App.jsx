import { Routes, Route } from 'react-router';
import './App.css'
import Office from './components/office'
import AvailableOffices from './components/AvailableOffices'
import LibraryButton from './components/LibraryButton';
import LibraryLogin from './components/LibraryLogin';
import LibraryBooks from './components/LibraryBooks'
import Doctor from './components/Doctor'


function App() {
  return(
    <Routes>
        <Route path="/" element={<LibraryLogin/>}/>
        <Route path="/office" element={<Office/>}/>
        <Route path="/offices" element={<AvailableOffices/>}/>
        <Route path="/available-offices" element={<AvailableOffices/>}/>
        <Route path="/books" element={<LibraryBooks/>}/>
        <Route path="*" element = {<LibraryLogin/>}/> 
        <Route path="/doctor" element={<Doctor/>}/>
        <Route path="/appointment" element={<LibraryBooks/>}/>
    </Routes>
  )    
}


export default App