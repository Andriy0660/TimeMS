import {
  Button,
  Card,
  CardActions,
  CardContent,
  Dialog, DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Switch, TextField,
  Typography
} from "@mui/material";
import {initializeConfig} from "../../config/config.js";
import useAppContext from "../../context/useAppContext.js";
import {useEffect, useState} from "react";
import ConfirmationModal from "../general/ConfirmationModal.jsx";

export default function ConfigJiraTab({isJiraEnabled, onSave, onDelete, setIsJiraEnabled, updateJiraConfig, jiraInstance}) {
  const {forceRender} = useAppContext();

  const [isConfigDialogOpen, setIsConfigDialogOpen] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);

  const [email, setEmail] = useState("");
  const [baseUrl, setBaseUrl] = useState("");
  const [token, setToken] = useState("");

  const [emailError, setEmailError] = useState("");
  const [baseUrlError, setBaseUrlError] = useState("");

  useEffect(() => {
    setEmail(jiraInstance.email || "");
    setBaseUrl(jiraInstance.baseUrl || "");
    setToken(jiraInstance.token || "");
  }, [jiraInstance]);

  const validateEmail = (value) => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(value)) {
      setEmailError("Invalid email");
    } else {
      setEmailError("");
    }
  };

  const validateBaseUrl = (value) => {
    const urlRegex = /^https:\/\/.*\.atlassian\.net$/;
    if (!urlRegex.test(value)) {
      setBaseUrlError("URL must be in this format: https://**.atlassian.net");
    } else {
      setBaseUrlError("");
    }
  };

  useEffect(() => {
    validateEmail(email)
  }, [email])

  useEffect(() => {
    validateBaseUrl(baseUrl)
  }, [baseUrl])

  const apiTokenError = token.length === 0;

  return (
    <>
    <Card className="mb-4">
      <CardContent>
        <Typography variant="h6">Jira Integration</Typography>
        <FormControlLabel
          className="mt-2"
          control={<Switch checked={isJiraEnabled} onChange={() => {
            setIsJiraEnabled(!isJiraEnabled);
          }} />}
          label="Enable Jira Synchronization"
        />
      </CardContent>
      <CardActions className="ml-2">
        <div className="flex flex-col">
          <div className="mb-4">
            <Button variant="contained" color="primary"
                    onClick={async () => {
                      await updateJiraConfig({isJiraEnabled});
                      await initializeConfig();
                      forceRender();
                    }}
            >Save</Button>
          </div>
          <div>
            <Button
              variant="contained"
              color="primary"
              onClick={() => {
                setIsConfigDialogOpen(true);
              }}
            >
              {jiraInstance.id ? "Edit" : "Add"} Jira Instance
            </Button>

            {jiraInstance.id && <Button
              variant="outlined"
              color="error"
              className="ml-2"
              onClick={() => setShowDeleteModal(true)}
            >
              Delete Jira Instance
            </Button>
            }
          </div>
        </div>
      </CardActions>
    </Card>
      <Dialog
        closeAfterTransition={false}
        open={isConfigDialogOpen}
        onClose={() => setIsConfigDialogOpen(false)}
      >
        <DialogTitle>
          Jira Instance
        </DialogTitle>
        <DialogContent>
          <TextField
            error={!!emailError}
            fullWidth
            margin="dense"
            label="Jira Email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
          <TextField
            error={!!baseUrlError}
            helperText={baseUrlError}
            fullWidth
            margin="dense"
            label="Base URL"
            value={baseUrl}
            onChange={(e) => setBaseUrl(e.target.value)}
          />
          <TextField
            error={apiTokenError}
            fullWidth
            margin="dense"
            label="API Token"
            type="password"
            value={token}
            onChange={(e) => setToken(e.target.value)}

          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setIsConfigDialogOpen(false)}>
            Cancel
          </Button>
          <Button
            disabled={!!emailError || !!baseUrlError || apiTokenError}
            onClick={() => {
              onSave({id: jiraInstance.id, email, baseUrl, token});
              setIsConfigDialogOpen(false);
            }}
            color="primary"
            variant="contained"
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
      <ConfirmationModal
        open={showDeleteModal}
        type="error"
        actionText="Delete"
        onConfirm={() => onDelete(jiraInstance.id)}
        onClose={() => {
          setShowDeleteModal(false);
        }}
      >
        Are you sure you want to delete jira instance?
      </ConfirmationModal>
    </>
  )
}