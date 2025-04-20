// gui/src/pages/ManagerPage.jsx
import { useState } from 'react';
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
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText,
  Divider
} from '@mui/material';
import managerApi from '../api/managerApi';
import useAppContext from '../context/useAppContext';
import LoadingPage from '../components/general/LoadingPage';

export default function ManagerPage() {
  const { addAlert } = useAppContext();
  const [tabValue, setTabValue] = useState(0);
  const [selectedUser, setSelectedUser] = useState(null);
  const [selectedTenant, setSelectedTenant] = useState(null);

  const { data: users, isLoading: isLoadingUsers } = useQuery({
  queryKey: ['manager', 'users'],
  queryFn: () => managerApi.getAllUsers(),
  onError: (error) => {
    addAlert({
      text: error.displayMessage || 'Failed to load users',
      type: 'error'
    });
  }
});

const { data: tenants, isLoading: isLoadingTenants } = useQuery({
  queryKey: ['manager', 'tenants'],
  queryFn: () => managerApi.getAllTenants(),
  onError: (error) => {
    addAlert({
      text: error.displayMessage || 'Failed to load tenants',
      type: 'error'
    });
  }
});

const { data: userInfo, isLoading: isLoadingUserInfo } = useQuery({
  queryKey: ['manager', 'user', selectedUser?.id],
  queryFn: () => selectedUser ? managerApi.getUserInfo(selectedUser.id) : null,
  enabled: !!selectedUser,
  onError: (error) => {
    addAlert({
      text: error.displayMessage || 'Failed to load user info',
      type: 'error'
    });
  }
});

const { data: tenantInfo, isLoading: isLoadingTenantInfo } = useQuery({
  queryKey: ['manager', 'tenant', selectedTenant?.id],
  queryFn: () => selectedTenant ? managerApi.getTenantInfo(selectedTenant.id) : null,
  enabled: !!selectedTenant,
  onError: (error) => {
    addAlert({
      text: error.displayMessage || 'Failed to load tenant info',
      type: 'error'
    });
  }
});


  const handleTabChange = (event, newValue) => {
    setTabValue(newValue);
    setSelectedUser(null);
    setSelectedTenant(null);
  };

  if (isLoadingUsers || isLoadingTenants) {
    return <LoadingPage />;
  }

  return (
    <div className="p-6">
      <Typography variant="h4" className="mb-4 text-center font-bold text-blue-500">Manager Dashboard</Typography>

      <Box sx={{ borderBottom: 1, borderColor: 'divider', marginBottom: 2 }}>
        <Tabs value={tabValue} onChange={handleTabChange}>
          <Tab label="Users" />
          <Tab label="Tenants" />
          <Tab label="User Details" />
          <Tab label="Tenant Details" />
        </Tabs>
      </Box>

      {/* Users Tab */}
      {tabValue === 0 && (
        <Paper elevation={3} className="p-4">
          <Typography variant="h6" className="mb-4">User List</Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Email</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Role</TableCell>
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
                        color="primary"
                        size="small"
                        onClick={() => {
                          setSelectedUser(user);
                          setTabValue(2);
                        }}
                      >
                        View Details
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {/* Tenants Tab */}
      {tabValue === 1 && (
        <Paper elevation={3} className="p-4">
          <Typography variant="h6" className="mb-4">Tenant List</Typography>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Schema Name</TableCell>
                  <TableCell>User</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {tenants?.map((tenant) => (
                  <TableRow key={tenant.id}>
                    <TableCell>{tenant.id}</TableCell>
                    <TableCell>{tenant.schemaName}</TableCell>
                    <TableCell>{tenant.userEmail || 'No user'}</TableCell>
                    <TableCell>
                      <Button
                        variant="outlined"
                        color="primary"
                        size="small"
                        onClick={() => {
                          setSelectedTenant(tenant);
                          setTabValue(3);
                        }}
                      >
                        View Details
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}

      {/* User Details Tab */}
      {tabValue === 2 && (
        <Paper elevation={3} className="p-4">
          <Typography variant="h6" className="mb-4">
            User Details {selectedUser ? `for: ${selectedUser.email}` : ''}
          </Typography>

          {!selectedUser ? (
            <Box display="flex" flexDirection="column" gap={2}>
              <Typography variant="body1">Please select a user from the Users tab</Typography>
              <Button
                variant="contained"
                color="primary"
                onClick={() => setTabValue(0)}
              >
                Go to Users List
              </Button>
            </Box>
          ) : isLoadingUserInfo ? (
            <LoadingPage />
          ) : (
            <Box display="flex" flexDirection="column" gap={3}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>User Information</Typography>
                  <List>
                    <ListItem>
                      <ListItemText primary="ID" secondary={userInfo.id} />
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemText primary="Email" secondary={userInfo.email} />
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemText primary="Name" secondary={`${userInfo.firstName} ${userInfo.lastName || ''}`} />
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemText primary="Status" secondary={userInfo.active ? 'Active' : 'Disabled'} />
                    </ListItem>
                    <Divider />
                    <ListItem>
                      <ListItemText
                        primary="Role"
                        secondary={
                          <Box display="flex" flexWrap="wrap" gap={1} mt={1}>
                            {userInfo.roles?.map((role, idx) => (
                              <span key={idx} className="inline-block bg-blue-100 text-blue-800 px-2 py-1 rounded">
                                {role}
                              </span>
                            ))}
                          </Box>
                        }
                      />
                    </ListItem>
                  </List>
                </CardContent>
              </Card>

              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>Tenants</Typography>
                  {userInfo.tenants?.length > 0 ? (
                    <TableContainer>
                      <Table size="small">
                        <TableHead>
                          <TableRow>
                            <TableCell>ID</TableCell>
                            <TableCell>Schema Name</TableCell>
                            <TableCell>Actions</TableCell>
                          </TableRow>
                        </TableHead>
                        <TableBody>
                          {userInfo.tenants?.map((tenant) => (
                            <TableRow key={tenant.id}>
                              <TableCell>{tenant.id}</TableCell>
                              <TableCell>{tenant.schemaName}</TableCell>
                              <TableCell>
                                <Button
                                  variant="outlined"
                                  color="primary"
                                  size="small"
                                  onClick={() => {
                                    setSelectedTenant(tenant);
                                    setTabValue(3);
                                  }}
                                >
                                  View Details
                                </Button>
                              </TableCell>
                            </TableRow>
                          ))}
                        </TableBody>
                      </Table>
                    </TableContainer>
                  ) : (
                    <Typography variant="body1">No tenants found for this user</Typography>
                  )}
                </CardContent>
              </Card>
            </Box>
          )}
        </Paper>
      )}

      {/* Tenant Details Tab */}
      {tabValue === 3 && (
        <Paper elevation={3} className="p-4">
          <Typography variant="h6" className="mb-4">
            Tenant Details {selectedTenant ? `for: ${selectedTenant.schemaName}` : ''}
          </Typography>

          {!selectedTenant ? (
            <Box display="flex" flexDirection="column" gap={2}>
              <Typography variant="body1">Please select a tenant from the Tenants tab</Typography>
              <Button
                variant="contained"
                color="primary"
                onClick={() => setTabValue(1)}
              >
                Go to Tenants List
              </Button>
            </Box>
          ) : isLoadingTenantInfo ? (
            <LoadingPage />
          ) : (
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>Tenant Information</Typography>
                <List>
                  <ListItem>
                    <ListItemText primary="ID" secondary={tenantInfo.id} />
                  </ListItem>
                  <Divider />
                  <ListItem>
                    <ListItemText primary="Schema Name" secondary={tenantInfo.schemaName} />
                  </ListItem>
                  <Divider />
                  <ListItem>
                    <ListItemText primary="Owner" secondary={tenantInfo.userEmail || 'No owner'} />
                  </ListItem>
                  <Divider />
                  <ListItem>
                    <ListItemText primary="Owner ID" secondary={tenantInfo.userId || 'None'} />
                  </ListItem>
                </List>

                {tenantInfo.userId && (
                  <Box display="flex" justifyContent="flex-end" mt={2}>
                    <Button
                      variant="contained"
                      color="primary"
                      onClick={() => {
                        const owner = users?.find(user => user.id === tenantInfo.userId);
                        if (owner) {
                          setSelectedUser(owner);
                          setTabValue(2);
                        }
                      }}
                    >
                      View Owner Details
                    </Button>
                  </Box>
                )}
              </CardContent>
            </Card>
          )}
        </Paper>
      )}
    </div>
  );
}