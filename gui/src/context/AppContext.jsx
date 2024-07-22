import {createContext} from "react";
import {closeSnackbar, SnackbarProvider, useSnackbar} from 'notistack';
import ClearOutlinedIcon from '@mui/icons-material/ClearOutlined';
import {IconButton} from "@mui/material";

const AppContext = createContext();

export const AppProvider = ({children}) => {
  const action = snackbarId => (
    <IconButton onClick={() => {
      closeSnackbar(snackbarId)
    }}>
      <ClearOutlinedIcon />
    </IconButton>

  );
  return (
    <SnackbarProvider
      preventDuplicate
      maxSnack={3}
      action={action}
    >
      <InnerAppProvider>{children}</InnerAppProvider>
    </SnackbarProvider>
  );
};

const InnerAppProvider = ({children}) => {
  const {enqueueSnackbar} = useSnackbar();

  const addAlert = ({type, text}) => {
    enqueueSnackbar(text, {variant: type});
  };

  return (
    <AppContext.Provider
      value={{
        addAlert,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export default AppContext;
