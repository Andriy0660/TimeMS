import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import {
  Typography,
  Tabs,
  Tab,
  Box,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Pagination
} from '@mui/material';
import adminApi from '../api/adminApi';
import managerApi from '../api/managerApi';
import useAppContext from '../context/useAppContext';
import LoadingPage from '../components/general/LoadingPage';

export default function AdminPage() {
  const { addAlert } = useAppContext();
  const [tabValue, setTabValue] = useState(0);
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedTenant, setSelectedTenant] = useState(null);
  const [page, setPage] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [roleToAdd, setRoleToAdd] = useState('ROLE_USER');

  const pageSize = 10;

  const { data: users, isLoading: isLoadingUsers, refetch: refetchUsers } = useQuery({
  queryKey: ['admin', 'users'],
  queryFn: () => managerApi.getAllUsers(),
  onError: (error) => {
    addAlert({
      text: error.displayMessage || 'Failed to load users',
      type: 'error'
    });
  }
});

const { data: tenants, isLoading: isLoadingTenants } = useQuery({
  queryKey: ['admin', 'tenants'],
  queryFn: () => managerApi.getAllTenants(),
  onError: (error) => {
    addAlert({
      text: error.displayMessage || 'Failed to load tenants',
      type: 'error'
    });
  }
});

const { data: roles, isLoading: isLoadingRoles } = useQuery({
  queryKey: ['admin', 'roles'],
  queryFn: () => adminApi.getAllRoles(),
  onError: (error) => {
    addAlert({
      text: error.displayMessage || 'Failed to load roles',
      type: 'error'
    });
  }
});

const { data: auditLogs, isLoading: isLoadingAuditLogs } = useQuery({
  queryKey: ['admin', 'audit', selectedTenant?.id, page],
  queryFn: () => selectedTenant ? adminApi.getAuditLogs(selectedTenant.id, page, pageSize) : null,
  enabled: !!selectedTenant,
  onError: (error) => {
    addAlert({
      text: error.displayMessage || 'Failed to load audit logs',
      type: 'error'
    });
  }
});

const { data: userAuditLogs, isLoading: isLoadingUserAuditLogs } = useQuery({
  queryKey: ['admin', 'userAudit', selectedUser?.id, page],
  queryFn: () => selectedUser ? adminApi.getUserAuditLogs(selectedUser.id, page, pageSize) : null,
  enabled: !!selectedUser && tabValue === 1,
  onError: (error) => {
    addAlert({
      text: error.displayMessage || 'Failed to load user audit logs',
      type: 'error'
    });
  }
});


  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
    setPage(0);
  };

  const handleToggleUserActive = async (userId) => {
    try {
      await adminApi.toggleUserActive(userId);
      addAlert({
        text: 'User active status toggled successfully',
        type: 'success'
      });
    } catch (error) {
      addAlert({
        text: error.displayMessage || 'Failed to toggle user active status',
        type: 'error'
      });
    } finally {
      refetchUsers();
    }
  };

  const handleAddRole = async () => {
    if (!selectedUser || !roleToAdd) return;
    console.log(roleToAdd)
    try {
      await adminApi.addRoleToUser(selectedUser.id, roleToAdd);
      addAlert({
        text: `Role ${roleToAdd} added to user successfully`,
        type: 'success'
      });
      setDialogOpen(false);
    } catch (error) {
      addAlert({
        text: error.displayMessage || 'Failed to add role to user',
        type: 'error'
      });
    } finally {
      refetchUsers()
    }
  };

  const handleRemoveRole = async (userId, roleName) => {
    try {
      await adminApi.removeRoleFromUser(userId, roleName);
      addAlert({
        text: `Role ${roleName} removed from user successfully`,
        type: 'success'
      });
    } catch (error) {
      addAlert({
        text: error.displayMessage || 'Failed to remove role from user',
        type: 'error'
      });
    }
  };

  if (isLoadingUsers || isLoadingTenants || isLoadingRoles) {
    return <LoadingPage />;
  }

  return (
    <div className="p-6">
      <Typography variant="h4" className="mb-4 text-center font-bold text-blue-500">Admin Panel</Typography>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', marginBottom: 2 }}>
        <Tabs value={tabValue} onChange={handleTabChange}>
          <Tab label="Users" />
          <Tab label="User Audit" />
        </Tabs>
      </Box>

      {tabValue === 0 && (
        <Paper elevation={3} className="p-4">
          <Typography variant="h6" className="mb-4">User Management</Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Roles</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users?.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>{user.id}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>{`${user.firstName} ${user.lastName || ''}`}</TableCell>
                    <TableCell>{user.active ? 'Active' : 'Disabled'}</TableCell>
                    <TableCell>
                      {user.roles?.map((role, idx) => (
                        <span key={idx} className="inline-block bg-blue-100 text-blue-800 px-2 py-1 rounded mr-1 mb-1">
                          {role}

                        </span>
                      ))}
                    </TableCell>
                    <TableCell>
                      <Button
                        variant="outlined"
                        color={user.active ? "error" : "success"}
                        size="small"
                        onClick={() => handleToggleUserActive(user.id)}
                        className="mr-2"
                      >
                        {user.active ? 'Disable' : 'Enable'}
                      </Button>
                      <Button
                        variant="outlined"
                        color="primary"
                        size="small"
                        onClick={() => {
                          setSelectedUser(user);
                          setDialogOpen(true);
                        }}
                      >
                        Change Role
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}


      {tabValue === 1 && (
        <Paper elevation={3} className="p-4">
          <Typography variant="h6" className="mb-4">
            User Audit Logs {selectedUser ? `for User: ${selectedUser.email}` : ''}
          </Typography>

          {(
            <Box display="flex" flexDirection="column" gap={2}>
              <Typography variant="body1">Please select a user:</Typography>
              <Select
                value={selectedUser?.id || ''}
                onChange={(e) => {
                  const userId = e.target.value;
                  setSelectedUser(users.find(u => u.id === userId) || null);
                }}
                displayEmpty
              >
                <MenuItem value="" disabled>Select a user</MenuItem>
                {users?.map(user => (
                  <MenuItem key={user.id} value={user.id}>{user.email}</MenuItem>
                ))}
              </Select>
            </Box>
          )}

          <TableContainer className="mt-8">
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>ID</TableCell>
                      <TableCell>Action</TableCell>
                      <TableCell>Entity</TableCell>
                      <TableCell>Details</TableCell>
                      <TableCell>IP Address</TableCell>
                      <TableCell>Timestamp</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {userAuditLogs?.content?.map((log) => (
                      <TableRow key={log.id}>
                        <TableCell>{log.id}</TableCell>
                        <TableCell>{log.action}</TableCell>
                        <TableCell>{log.entityType} {log.entityId ? `#${log.entityId}` : ''}</TableCell>
                        <TableCell>{log.details}</TableCell>
                        <TableCell>{log.ipAddress}</TableCell>
                        <TableCell>{new Date(log.timestamp).toLocaleString()}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
              <Box display="flex" justifyContent="center" mt={3}>
                <Pagination
                  count={userAuditLogs?.totalPages || 0}
                  page={page + 1}
                  onChange={(e, value) => setPage(value - 1)}
                />
              </Box>
        </Paper>
      )}

      {/* Add Role Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>Change Role to User</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ marginTop: 2 }}>
            {/*<InputLabel>Role</InputLabel>*/}
            <Select
              value={roleToAdd}
              onChange={(e) => setRoleToAdd(e.target.value)}
            >
              {roles?.map((role) => (
                <MenuItem key={role.id} value={role.name}>
                  {role.name}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleAddRole} color="primary" variant="contained">
            Change Role
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}