import Button from "@mui/material/Button";
import fileService from "../service/fileService.js";
import {useRef, useState} from "react";
import timeLogProcessingService from "../service/timeLogProcessingService.js";
import {CircularProgress} from "@mui/material";
import useAsyncCall from "../hooks/useAsyncCall.js";
import useAppContext from "../context/useAppContext.js";

export default function ImportButton({className, onMerge}) {
  const fileInputRef = useRef(null);
  const [isReading, setIsReading] = useState(false);
  const {addAlert} = useAppContext();
  const {execute: handleMerge, isExecuting: isMerging} = useAsyncCall({
    fn: onMerge
  })

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (file && file.type !== "text/plain") {
      addAlert({
        text: "Choose text file!",
        type: "error"
      });
      fileInputRef.current.value = "";
      return;
    }
    if (file) {
      const reader = new FileReader();
      reader.onload = async (e) => {
        try {
          const fileContent = e.target.result;
          const dateGroups = timeLogProcessingService.group(fileService.parseTimeLogs(fileContent), ["date"]);
          await handleMerge({dateGroups: dateGroups.data})
        } catch (error) {
          addAlert({
            text: error.message,
            type: "error"
          });
          console.error(error)
        } finally {
          fileInputRef.current.value = "";
        }
      };
      reader.onloadstart = () => setIsReading(true);
      reader.onloadend = () => setIsReading(false);
      reader.readAsText(file);
    }
  };

  return (
    <Button component="label" className={className} variant="outlined">
      {isReading || isMerging ? <CircularProgress size={25} /> : "Import"}
      <input
        hidden
        type="file"
        accept=".txt"
        onChange={handleFileChange}
        ref={fileInputRef}
      />
    </Button>
  );
}
