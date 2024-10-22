import Button from "@mui/material/Button";
import fileService from "../../service/fileService.js";
import dateTimeService from "../../service/dateTimeService.js";
import useAppContext from "../../context/useAppContext.js";

export default function ExportButton({processedTimeLogsArray, className}) {
  const {date, mode} = useAppContext();

  const saveFile = async () => {
    const formattedText = fileService.convertToTxt(processedTimeLogsArray);
    const blob = new Blob([formattedText], {type: "text/plain"});
    const a = document.createElement('a');
    a.download = `${dateTimeService.getFormattedDate(date)}-${mode}`
    a.href = URL.createObjectURL(blob);
    a.addEventListener("click", (e) => {
      setTimeout(() => URL.revokeObjectURL(a.href), 30 * 1000);
    });
    a.click();
  };

  return (
    <Button
      className={className}
      variant="outlined"
      onClick={saveFile}
    >
      Export
    </Button>
  )
}