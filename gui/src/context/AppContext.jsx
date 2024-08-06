import {createContext} from "react";
import {ToastContainer, toast} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const AppContext = createContext();

export const AppProvider = ({children}) => {
  const addAlert = ({type, text, action, id}) => {
    return toast[type](text, {closeButton: action, toastId: id});
  };
  const removeAlert = (id) => {
    return toast.dismiss(id);
  };
  const showAlertSeconds = 5000;

  return (
    <>
      <ToastContainer
        position="bottom-left"
        autoClose={showAlertSeconds}
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
        addAlert,
        removeAlert,
        showAlertSeconds
      }}>
        {children}
      </AppContext.Provider>
    </>
  );
};

export default AppContext;
