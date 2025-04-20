import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  MenuItem,
  Select,
  Toolbar,
  Tooltip,
  Typography
} from "@mui/material";
import MenuIcon from '@mui/icons-material/Menu';
import {useState} from "react";
import {Link, useNavigate} from "react-router-dom";
import MonthPicker from "./month/MonthPicker..jsx";
import WeekPicker from "./week/WeekPicker.jsx";
import DayPicker from "./day/DayPicker.jsx";
import useAppContext from "../context/useAppContext.js";
import useDateInUrl from "../hooks/useDateInUrl.js";
import dayjs from "dayjs";
import SettingsBackupRestoreIcon from "@mui/icons-material/SettingsBackupRestore.js";
import useViewChanger from "../hooks/useViewChanger.js";
import SyncWorklogsButton from "./sync/SyncWorklogsButton.jsx";
import {viewMode} from "../consts/viewMode.js";
import {isJiraSyncingEnabled} from "../config/config.js";
import ConfirmationModal from "./general/ConfirmationModal.jsx";
import useAsyncCall from "../hooks/useAsyncCall.js";
import authService from "../service/authService.js";

export default function NavBar() {
  const [open, setOpen] = useState(false);
  const {date, setDate, mode, addAlert} = useAppContext();
  const [showLogoutModal, setShowLogoutModal] = useState(false);

  const navigate = useNavigate();

  useDateInUrl(date);
  const {changeView} = useViewChanger();

  const modeDatePickerConfig = {
    DAY: <DayPicker buttonColor="white" />,
    WEEK: <WeekPicker buttonColor="white" />,
    MONTH: <MonthPicker buttonColor="white" />,
  };

  const toggleMenu = (newOpen) => () => {
    setOpen(newOpen);
  };

  const {execute: handleLogOut} = useAsyncCall({
    fn: authService.logOut,
    onSuccess: () => navigate("/app/login"),
    onError: async (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Logging out failed:", error);
    }
  })

  const DrawerList = (
    <Box sx={{width: 250}} onClick={toggleMenu(false)}>
      <List>
        <ListItem disablePadding>
          <Link to="/app/timelog" onClick={() => changeView(viewMode.DAY)} className="text-inherit no-underline w-full">
            <ListItemButton>
              <ListItemText primary="TimeLog" />
            </ListItemButton>
          </Link>
        </ListItem>

        {isJiraSyncingEnabled && <ListItem disablePadding>
          <Link to="/app/syncWorklogs" className="text-inherit no-underline w-full">
            <ListItemButton>
              <ListItemText primary="Sync Worklogs" />
            </ListItemButton>
          </Link>
        </ListItem>
        }

        {/*<ListItem disablePadding>*/}
        {/*  <Link to="/app/info" className="text-inherit no-underline w-full">*/}
        {/*    <ListItemButton>*/}
        {/*      <ListItemText primary="Info" />*/}
        {/*    </ListItemButton>*/}
        {/*  </Link>*/}
        {/*</ListItem>*/}

        <ListItem disablePadding>
          <Link to="/app/config" className="text-inherit no-underline w-full">
            <ListItemButton>
              <ListItemText primary="Configuration" />
            </ListItemButton>
          </Link>
        </ListItem>

        <ListItem disablePadding>
          <ListItemButton onClick={() => setShowLogoutModal(true)}>
            <ListItemText primary="Logout" />
          </ListItemButton>
        </ListItem>

      </List>

    </Box>
  );

  return (
    <AppBar position="static">
      <Toolbar>
        <IconButton
          onClick={toggleMenu(true)}
          size="large"
          edge="start"
          sx={{mr: 2}}
          color="inherit"
        >
          <MenuIcon />
        </IconButton>
        <Typography variant="h6">
          Time Craft
        </Typography>
        <Select
          className="mx-8 bg-white"
          size="small"
          inputProps={{"aria-label": "Without label"}}
          value={mode}
          onChange={(event) => {
            changeView(event.target.value);
          }}
          autoWidth
        >
          <MenuItem value={viewMode.DAY}>Day</MenuItem>
          <MenuItem value={viewMode.WEEK}>Week</MenuItem>
          <MenuItem value={viewMode.MONTH}>Month</MenuItem>
        </Select>
        {modeDatePickerConfig[mode]}
        <Tooltip title="Reset">
          <IconButton
            onClick={() => setDate(dayjs())}
            variant="outlined"
            className="text-white"
          >
            <SettingsBackupRestoreIcon />
          </IconButton>
        </Tooltip>
        {isJiraSyncingEnabled && <SyncWorklogsButton className="mx-4">Sync Worklogs</SyncWorklogsButton>}
      </Toolbar>
      <Drawer open={open} onClose={toggleMenu(false)}>
        {DrawerList}
      </Drawer>
      <ConfirmationModal
        open={showLogoutModal}
        type="warning"
        actionText="Logout"
        onConfirm={handleLogOut}
        onClose={() => setShowLogoutModal(false)}
      >
        Are you sure you want to log out of your account?
      </ConfirmationModal>
    </AppBar>
  );
}