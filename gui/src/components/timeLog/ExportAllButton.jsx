import Button from "@mui/material/Button";
import FileDownloadIcon from '@mui/icons-material/FileDownload';
import axios from "axios";
import dateTimeService from "../../service/dateTimeService.js";
import useAppContext from "../../context/useAppContext.js";
import { useState } from "react";
import { CircularProgress, Tooltip } from "@mui/material";
import { isExternalServiceSyncingEnabled, isJiraSyncingEnabled } from "../../config/config.js";
import timeLogApi from "../../api/timeLogApi.js";

export default function ExportAllButton({ className }) {
  const { addAlert, date } = useAppContext();
  const [isExporting, setIsExporting] = useState(false);

  const handleExport = async () => {
    setIsExporting(true);
    try {
      const response = await timeLogApi.exportAll();

      // Create a new Blob with the response data
      const blob = new Blob([JSON.stringify(response, null, 2)], { type: 'application/json' });

      // Create a download link
      const a = document.createElement('a');
      a.download = `timecraft-export-${dateTimeService.getFormattedDate(date)}.json`;
      a.href = URL.createObjectURL(blob);
      a.addEventListener("click", (e) => {
        setTimeout(() => URL.revokeObjectURL(a.href), 30 * 1000);
      });
      a.click();

      addAlert({
        text: "Successfully exported all logs",
        type: "success"
      });
    } catch (error) {
      addAlert({
        text: error.response?.data?.detail || "Error exporting logs",
        type: "error"
      });
      console.error("Error exporting logs:", error);
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <Tooltip title="Export all logs (TimeLogs, Worklogs, and External Time Logs)">
      <Button
        className={className}
        variant="outlined"
        color="primary"
        onClick={handleExport}
        startIcon={isExporting ? <CircularProgress size={18} /> : <FileDownloadIcon />}
        disabled={isExporting}
      >
        Export All
      </Button>
    </Tooltip>
  );
}