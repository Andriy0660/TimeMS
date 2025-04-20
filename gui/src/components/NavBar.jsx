// gui/src/components/general/Navbar.jsx
import { useEffect, useState } from "react";
import {
  AppBar,
  Box,
  Toolbar,
  IconButton,
  Typography,
  Menu,
  Container,
  Avatar,
  Button,
  Tooltip,
  MenuItem,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider, Select,
} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import ViewWeekIcon from "@mui/icons-material/ViewWeek";
import CalendarMonthIcon from "@mui/icons-material/CalendarMonth";
import PublishIcon from "@mui/icons-material/Publish";
import InfoIcon from "@mui/icons-material/Info";
import LogoutIcon from "@mui/icons-material/Logout";
import SettingsIcon from "@mui/icons-material/Settings";
import SupervisorAccountIcon from "@mui/icons-material/SupervisorAccount";
import AdminPanelSettingsIcon from "@mui/icons-material/AdminPanelSettings";
import { useNavigate } from "react-router-dom";
import authService from "../service/authService.js";
import useAppContext from "../context/useAppContext.js";
import useAuthInfo from "../hooks/useAuthInfo.js";
import SyncWorklogsButton from "./sync/SyncWorklogsButton.jsx";
import DayPicker from "./day/DayPicker.jsx";
import WeekPicker from "./week/WeekPicker.jsx";
import MonthPicker from "./month/MonthPicker..jsx";
import {viewMode} from "../consts/viewMode.js";
import dayjs from "dayjs";
import useViewChanger from "../hooks/useViewChanger.js";
import {isJiraSyncingEnabled} from "../config/config.js";
import SettingsBackupRestoreIcon from "@mui/icons-material/SettingsBackupRestore.js";


function Navbar() {
  const navigate = useNavigate();
  const {date, setDate, mode, addAlert} = useAppContext();
  const {changeView} = useViewChanger();

  const { user } = useAuthInfo();
  const [anchorElUser, setAnchorElUser] = useState(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  
  const isAdmin = user && authService.hasRole(user, "ROLE_ADMIN");
  const isManager = user && (authService.hasRole(user, "ROLE_MANAGER") || authService.hasRole(user, "ROLE_ADMIN"));
  const isRegularUser = user && !isAdmin && !isManager;

  const handleOpenUserMenu = (event) => {
    setAnchorElUser(event.currentTarget);
  };

  const handleCloseUserMenu = () => {
    setAnchorElUser(null);
  };

  const modeDatePickerConfig = {
    DAY: <DayPicker buttonColor="white" />,
    WEEK: <WeekPicker buttonColor="white" />,
    MONTH: <MonthPicker buttonColor="white" />,
  };

  const logOut = async () => {
    handleCloseUserMenu();
    try {
      await authService.logOut();
      navigate("/app/login");
      addAlert({
        text: "You have successfully logged out",
        type: "success",
      });
    } catch (error) {
      addAlert({
        text: error.displayMessage || "Failed to log out",
        type: "error",
      });
    }
  };

  // Базові пункти меню для звичайних користувачів
  const regularUserItems = [
    { name: "Time Log", icon: <AccessTimeIcon />, path: "/app/timelog" },
    // { name: "Week View", icon: <ViewWeekIcon />, path: "/app/weekview" },
    // { name: "Month View", icon: <CalendarMonthIcon />, path: "/app/monthview" },
    { name: "Sync Worklogs", icon: <PublishIcon />, path: "/app/syncWorklogs" },
    { name: "Setting", icon: <SettingsIcon />, path: "/app/config" },
  ];

  // Пункти меню для менеджерів
  const managerItems = [
    { name: "Manager Dashboard", icon: <SupervisorAccountIcon />, path: "/app/manager" }
  ];

  // Пункти меню для адміністраторів
  const adminItems = [
    { name: "Admin Panel", icon: <AdminPanelSettingsIcon />, path: "/app/admin" }
  ];

  // Визначаємо, які пункти меню відображати залежно від ролі
  let navItems = [];

  if (isAdmin) {
    navItems = [...adminItems, ...managerItems];
  } else if (isManager) {
    navItems = [...managerItems];
  } else {
    navItems = [...regularUserItems];
  }

  // Показуємо дані про День/Тиждень/Місяць та елементи навігації тільки для звичайних користувачів
  const showDateControls = isRegularUser;

  return (
    <AppBar position="sticky" sx={{ zIndex: 100 }}>
      <Container maxWidth="xl">
        <Toolbar disableGutters>
          <IconButton
            size="large"
            onClick={() => setDrawerOpen(!drawerOpen)}
            color="inherit"
          >
            <MenuIcon />
          </IconButton>
          <Typography
            variant="h6"
            noWrap
            component="a"
            href="/"
            sx={{
              ml: 1,
              display: { md: "flex" },
              fontWeight: 800,
              color: "inherit",
              textDecoration: "none",
            }}
          >
            TimeCraft
          </Typography>

          <Box sx={{ flexGrow: 1 }} />

          {showDateControls && (
            <>
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
            </>
          )}

          <Box sx={{ flexGrow: 0 }}>
            <Tooltip title="Open settings">
              <IconButton onClick={handleOpenUserMenu} sx={{ p: 0 }}>
                <Avatar alt={user?.firstName || "User"} src="/static/images/avatar/2.jpg" />
              </IconButton>
            </Tooltip>
            <Menu
              sx={{ mt: "45px" }}
              id="menu-appbar"
              anchorEl={anchorElUser}
              anchorOrigin={{
                vertical: "top",
                horizontal: "right",
              }}
              keepMounted
              transformOrigin={{
                vertical: "top",
                horizontal: "right",
              }}
              open={Boolean(anchorElUser)}
              onClose={handleCloseUserMenu}
            >
              {/*<MenuItem*/}
              {/*  onClick={() => {*/}
              {/*    navigate("/app/config");*/}
              {/*    handleCloseUserMenu();*/}
              {/*  }}*/}
              {/*>*/}
              {/*  <ListItemIcon>*/}
              {/*    <SettingsIcon fontSize="small" />*/}
              {/*  </ListItemIcon>*/}
              {/*  <Typography textAlign="center">Settings</Typography>*/}
              {/*</MenuItem>*/}
              <MenuItem onClick={logOut}>
                <ListItemIcon>
                  <LogoutIcon fontSize="small" />
                </ListItemIcon>
                <Typography textAlign="center">Logout</Typography>
              </MenuItem>
            </Menu>
          </Box>
      </Toolbar>
      </Container>

      <Drawer
        anchor="left"
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
      >
        <Box
          sx={{ width: 250 }}
          role="presentation"
          onClick={() => setDrawerOpen(false)}
        >
          <List>
            {navItems.map((item) => (
              <ListItem key={item.name} disablePadding>
                <ListItemButton onClick={() => navigate(item.path)}>
                  <ListItemIcon>{item.icon}</ListItemIcon>
                  <ListItemText primary={item.name} />
                </ListItemButton>
              </ListItem>
            ))}
          </List>
          <Divider />
          {/*<List>*/}
          {/*  <ListItem disablePadding>*/}
          {/*    <ListItemButton onClick={() => navigate("/app/config")}>*/}
          {/*      <ListItemIcon>*/}
          {/*        <SettingsIcon />*/}
          {/*      </ListItemIcon>*/}
          {/*      <ListItemText primary="Settings" />*/}
          {/*    </ListItemButton>*/}
          {/*  </ListItem>*/}
          {/*</List>*/}
        </Box>
      </Drawer>
    </AppBar>
  );
}

export default Navbar;