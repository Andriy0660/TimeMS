import {createContext} from "react";
import {ToastContainer, toast} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const AppContext = createContext();

export const AppProvider = ({children}) => {
  const addAlert = ({type, text}) => {
    return toast[type](text);
  };

  return (
    <>
      <ToastContainer
        position="bottom-left"
        autoClose={5000}
        limit={3}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick={false}
        rtl={false}
        pauseOnFocusLoss={false}
        draggable
        pauseOnHover={false}
        theme="light"
        transition: Bounce
      />
      <AppContext.Provider value={{
        addAlert
      }}>
        {children}
      </AppContext.Provider>
    </>
  );
};

export default AppContext;
