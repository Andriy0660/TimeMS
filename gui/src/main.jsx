import React from 'react'
import {createRoot} from 'react-dom/client'
import App from './App.jsx'
import {createTheme, CssBaseline, StyledEngineProvider, ThemeProvider} from "@mui/material";
import './index.css'
import {AppProvider} from "./context/AppContext.jsx";

const rootElement = document.getElementById("root");

const root = createRoot(rootElement);

const theme = createTheme({
  components: {
    MuiPopover: {
      defaultProps: {
        container: rootElement,
      },
    },
    MuiPopper: {
      defaultProps: {
        container: rootElement,
      },
    },
    MuiDialog: {
      defaultProps: {
        container: rootElement,
      },
    },
    MuiModal: {
      defaultProps: {
        container: rootElement,
      },
    },
  },
});

root.render(
  <React.StrictMode>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <CssBaseline>
          <AppProvider>
            <App />
          </AppProvider>
        </CssBaseline>
      </ThemeProvider>
    </StyledEngineProvider>
  </React.StrictMode>,
)
