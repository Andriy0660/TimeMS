import Button from "@mui/material/Button";
import FileUploadIcon from '@mui/icons-material/FileUpload';
import axios from "axios";
import useAppContext from "../../context/useAppContext.js";
import { useRef, useState } from "react";
import { CircularProgress, Tooltip } from "@mui/material";
import timeLogApi from "../../api/timeLogApi.js";

export default function ImportAllButton({ className }) {
  const { addAlert } = useAppContext();
  const [isImporting, setIsImporting] = useState(false);
  const fileInputRef = useRef(null);

  const handleFileChange = async (event) => {
    const file = event.target.files[0];
    if (!file) return;

    if (file.type !== "application/json") {
      addAlert({
        text: "Please select a JSON file",
        type: "error"
      });
      fileInputRef.current.value = "";
      return;
    }

    setIsImporting(true);
    const reader = new FileReader();

    reader.onload = async (e) => {
      try {
        const content = JSON.parse(e.target.result);

        // Validate the content structure
        if (!content.timeLogs && !content.worklogs && !content.externalTimeLogs) {
          throw new Error("Invalid file format. File should contain timeLogs, worklogs or externalTimeLogs");
        }

        await timeLogApi.importAll(content);

        addAlert({
          text: "Successfully imported logs",
          type: "success"
        });
      } catch (error) {
        addAlert({
          text: error.response?.data?.detail || error.message || "Error importing logs",
          type: "error"
        });
        console.error("Error importing logs:", error);
      } finally {
        setIsImporting(false);
        fileInputRef.current.value = "";
      }
    };

    reader.onerror = () => {
      addAlert({
        text: "Error reading file",
        type: "error"
      });
      setIsImporting(false);
      fileInputRef.current.value = "";
    };

    reader.readAsText(file);
  };

  return (
    <Tooltip title="Import all logs (TimeLogs, Worklogs, and External Time Logs)">
      <Button
        className={className}
        variant="outlined"
        color="primary"
        component="label"
        startIcon={isImporting ? <CircularProgress size={18} /> : <FileUploadIcon />}
        disabled={isImporting}
      >
        Import All
        <input
          hidden
          type="file"
          accept=".json"
          onChange={handleFileChange}
          ref={fileInputRef}
        />
      </Button>
    </Tooltip>
  );
}