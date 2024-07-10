import React from 'react'
import {createRoot} from 'react-dom/client'
import App from './App.jsx'
import {createTheme, CssBaseline, StyledEngineProvider, ThemeProvider} from "@mui/material";
import './index.css'
import {AppProvider} from "./context/AppContext.jsx";
import {QueryClient, QueryClientProvider} from "@tanstack/react-query";

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

const queryClient = new QueryClient();
root.render(
  <React.StrictMode>
    <StyledEngineProvider injectFirst>
      <ThemeProvider theme={theme}>
        <CssBaseline>
          <QueryClientProvider client={queryClient}>
            <AppProvider>
              <App />
            </AppProvider>
          </QueryClientProvider>
        </CssBaseline>
      </ThemeProvider>
    </StyledEngineProvider>
  </React.StrictMode>,
)
