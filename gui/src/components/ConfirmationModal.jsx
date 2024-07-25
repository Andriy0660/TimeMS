import Spinner from "./icons/Spinner.jsx";
import {Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle} from "@mui/material";
import Button from "@mui/material/Button";
import {ExclamationMark} from "./icons/ExclamationMark.jsx";
import {useState} from "react";

export default function ConfirmationModal({
  open,
  type,
  children,
  actionText,
  onConfirm,
  onCancel,
  onClose,
}) {

  const [isLoading, setIsLoading] = useState(false);

  if (!open) {
    return null;
  }

  async function handleConfirm() {
    setIsLoading(true);
    try {
      await onConfirm();
    } catch (error) {
      console.error("Error:", error);
    } finally {
      setIsLoading(false);
      onClose()
    }
  }

  const typeColorConfig = {
    error: "error",
    info: "primary",
  };
  const color = typeColorConfig[type] || "primary";

  return (
    <Dialog open={true} onClose={onClose} onClick={e => e.stopPropagation()}>
      <DialogTitle className="pt-4 flex justify-center">
        <ExclamationMark />
      </DialogTitle>

      <DialogContent>
        <DialogContentText>
          {children}
        </DialogContentText>
      </DialogContent>

      <DialogActions className="mb-2 mr-2">
        <Button
          variant="outlined"
          color="info"
          size="large"
          onClick={() => {
            if (onCancel) {
              onCancel();
            }
            onClose();
          }}
        >
          Cancel
        </Button>
        {isLoading ? <Spinner /> : <Button
          color={color}
          variant="contained"
          size="large"
          onClick={handleConfirm}
        >
          {actionText}
        </Button>
        }
      </DialogActions>
    </Dialog>
  );
}
