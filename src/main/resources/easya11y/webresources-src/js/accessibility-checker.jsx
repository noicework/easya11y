import React, { useState, useEffect, useCallback } from 'react';
import ReactDOM from 'react-dom/client';
import {
  ThemeProvider,
  GlobalStyles,
  Container,
  Card,
  Button,
  Select,
  TextField,
  Badge,
  Chip,
  Alert,
  Progress,
  Tabs,
  Tab,
  Table,
  TableHead,
  TableBody,
  TableRow,
  TableCell,
  IconButton,
  Typography,
  Box,
  Stack,
  Grid,
  Divider,
  Modal,
  Accordion,
  AccordionSummary,
  AccordionDetails
} from '@magnolia-services/mgnl-ds-react';
import { createScanner } from './accessibility-scanner';

// Icons (using Unicode symbols as placeholders - replace with actual icons from DS)
const Icons = {
  scan: '▶',
  scanAll: '⊞',
  export: '⤓',
  filter: '⊙',
  expand: '⌄',
  collapse: '⌃',
  search: '⌕',
  refresh: '↻',
  close: '✕',
  warning: '⚠',
  error: '✗',
  success: '✓',
  info: 'ℹ'
};

// Configuration
const baseUrl = window.location.origin;
const pathParts = window.location.pathname.split('/');
const contextPath = pathParts[1] === 'magnoliaAuthor' ? '/magnoliaAuthor' : '';
const apiBase = `${baseUrl}${contextPath}/.rest`;

// Initialize scanner
const scanner = createScanner(apiBase);

// Main App Component
function AccessibilityCheckerApp() {
  // State
  const [pages, setPages] = useState([]);
  const [scanResults, setScanResults] = useState([]);
  const [selectedPage, setSelectedPage] = useState('');
  const [searchTerm, setSearchTerm] = useState('');
  const [severityFilter, setSeverityFilter] = useState('');
  const [wcagFilter, setWcagFilter] = useState('');
  const [sortBy, setSortBy] = useState('score-asc');
  const [loading, setLoading] = useState(false);
  const [scanning, setScanning] = useState(false);
  const [scanProgress, setScanProgress] = useState(null);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [activeTab, setActiveTab] = useState(0);
  const [showFilters, setShowFilters] = useState(false);
  const [selectedResult, setSelectedResult] = useState(null);
  const [stats, setStats] = useState(null);

  // Load initial data
  useEffect(() => {
    loadPages();
    loadScanResults();
  }, []);

  // Load pages
  const loadPages = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${apiBase}/easya11y/pages?includeStatus=true`);
      if (!response.ok) throw new Error('Failed to load pages');
      
      const data = await response.json();
      setPages(data.items || []);
    } catch (err) {
      setError(`Error loading pages: ${err.message}`);
    } finally {
      setLoading(false);
    }
  };

  // Load scan results
  const loadScanResults = async () => {
    try {
      const params = new URLSearchParams();
      if (selectedPage) params.append('pagePath', selectedPage);
      if (severityFilter) params.append('severity', severityFilter);
      if (wcagFilter) params.append('wcagLevel', wcagFilter);
      
      const response = await fetch(`${apiBase}/easya11y/results?${params}`);
      if (!response.ok) throw new Error('Failed to load scan results');
      
      const data = await response.json();
      setScanResults(data.results || []);
      setStats(data.summary || null);
    } catch (err) {
      setError(`Error loading scan results: ${err.message}`);
    }
  };

  // Scan single page
  const scanPage = async (pagePath) => {
    try {
      setScanning(true);
      setError(null);
      
      // Initiate scan
      const initResponse = await fetch(`${apiBase}/easya11y/scan/initiate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ pagePath })
      });
      
      if (!initResponse.ok) throw new Error('Failed to initiate scan');
      const pageData = await initResponse.json();
      
      // Perform scan
      const results = await scanner.scanPage(pageData);
      setSuccess(`Successfully scanned ${pageData.pageTitle}`);
      
      // Reload results
      await loadScanResults();
      await loadPages();
    } catch (err) {
      setError(`Scan failed: ${err.message}`);
    } finally {
      setScanning(false);
    }
  };

  // Scan all pages
  const scanAllPages = async () => {
    if (!confirm('This will scan all pages. This may take a while. Continue?')) {
      return;
    }

    try {
      setScanning(true);
      setError(null);
      
      const pagesToScan = pages.filter(p => p.type === 'mgnl:page');
      
      await scanner.scanMultiplePages(pagesToScan, (progress) => {
        setScanProgress(progress);
      });
      
      setSuccess('All pages scanned successfully');
      setScanProgress(null);
      
      // Reload results
      await loadScanResults();
      await loadPages();
    } catch (err) {
      setError(`Scan all failed: ${err.message}`);
    } finally {
      setScanning(false);
      setScanProgress(null);
    }
  };

  // Export results
  const exportResults = () => {
    const params = new URLSearchParams();
    if (selectedPage) params.append('pagePath', selectedPage);
    
    window.open(`${apiBase}/easya11y/results/export/csv?${params}`, '_blank');
  };

  // Filter results
  const filteredResults = scanResults.filter(result => {
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      return (
        result.pageTitle?.toLowerCase().includes(searchLower) ||
        result.pagePath?.toLowerCase().includes(searchLower)
      );
    }
    return true;
  });

  // Sort results
  const sortedResults = [...filteredResults].sort((a, b) => {
    switch (sortBy) {
      case 'score-asc':
        return (a.score || 0) - (b.score || 0);
      case 'score-desc':
        return (b.score || 0) - (a.score || 0);
      case 'date-desc':
        return (b.scanDate || 0) - (a.scanDate || 0);
      case 'violations-desc':
        return (b.violationCount || 0) - (a.violationCount || 0);
      default:
        return 0;
    }
  });

  // Get severity color
  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'critical': return 'error';
      case 'serious': return 'warning';
      case 'moderate': return 'info';
      case 'minor': return 'default';
      default: return 'default';
    }
  };

  // Get score color
  const getScoreColor = (score) => {
    if (score >= 90) return 'success';
    if (score >= 70) return 'warning';
    return 'error';
  };

  // Format date
  const formatDate = (timestamp) => {
    if (!timestamp) return '-';
    const date = new Date(timestamp);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <ThemeProvider>
      <GlobalStyles />
      <Container maxWidth="xl" sx={{ py: 3 }}>
        {/* Header */}
        <Box sx={{ mb: 3 }}>
          <Typography variant="h1" sx={{ mb: 2 }}>
            Accessibility Checker
          </Typography>
          
          {/* Stats Summary */}
          {stats && (
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    Total Scans
                  </Typography>
                  <Typography variant="h3">{stats.totalScans}</Typography>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    Average Score
                  </Typography>
                  <Typography variant="h3" color={getScoreColor(stats.averageScore)}>
                    {stats.averageScore?.toFixed(1)}
                  </Typography>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    Critical Issues
                  </Typography>
                  <Typography variant="h3" color="error">
                    {stats.totalCritical}
                  </Typography>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                  <Typography variant="body2" color="text.secondary">
                    Perfect Score Pages
                  </Typography>
                  <Typography variant="h3" color="success">
                    {stats.perfectScorePages}
                  </Typography>
                </Card>
              </Grid>
            </Grid>
          )}
        </Box>

        {/* Controls */}
        <Card sx={{ mb: 3, p: 2 }}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} md={3}>
              <TextField
                fullWidth
                placeholder="Search pages or issues..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                InputProps={{
                  startAdornment: <Box sx={{ mr: 1 }}>{Icons.search}</Box>
                }}
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <Select
                fullWidth
                value={selectedPage}
                onChange={(e) => setSelectedPage(e.target.value)}
                displayEmpty
              >
                <option value="">All Pages</option>
                {pages.map(page => (
                  <option key={page.path} value={page.path}>
                    {page.title || page.name}
                  </option>
                ))}
              </Select>
            </Grid>
            <Grid item xs={12} md={2}>
              <Select
                fullWidth
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
              >
                <option value="score-asc">Worst Score First</option>
                <option value="score-desc">Best Score First</option>
                <option value="date-desc">Recently Scanned</option>
                <option value="violations-desc">Most Issues</option>
              </Select>
            </Grid>
            <Grid item xs={12} md={4}>
              <Stack direction="row" spacing={1}>
                <Button
                  variant="contained"
                  color="primary"
                  onClick={() => selectedPage && scanPage(selectedPage)}
                  disabled={!selectedPage || scanning}
                  startIcon={Icons.scan}
                >
                  Scan Page
                </Button>
                <Button
                  variant="contained"
                  color="primary"
                  onClick={scanAllPages}
                  disabled={scanning}
                  startIcon={Icons.scanAll}
                >
                  Scan All
                </Button>
                <Button
                  variant="outlined"
                  onClick={() => setShowFilters(!showFilters)}
                  startIcon={Icons.filter}
                >
                  Filters
                </Button>
                <Button
                  variant="outlined"
                  color="success"
                  onClick={exportResults}
                  startIcon={Icons.export}
                >
                  Export
                </Button>
              </Stack>
            </Grid>
          </Grid>

          {/* Filters */}
          {showFilters && (
            <Box sx={{ mt: 2, pt: 2, borderTop: 1, borderColor: 'divider' }}>
              <Grid container spacing={2}>
                <Grid item xs={12} md={4}>
                  <Select
                    fullWidth
                    value={severityFilter}
                    onChange={(e) => setSeverityFilter(e.target.value)}
                    displayEmpty
                  >
                    <option value="">All Severities</option>
                    <option value="critical">Critical</option>
                    <option value="serious">Serious</option>
                    <option value="moderate">Moderate</option>
                    <option value="minor">Minor</option>
                  </Select>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Select
                    fullWidth
                    value={wcagFilter}
                    onChange={(e) => setWcagFilter(e.target.value)}
                    displayEmpty
                  >
                    <option value="">All WCAG Levels</option>
                    <option value="A">Level A</option>
                    <option value="AA">Level AA</option>
                    <option value="AAA">Level AAA</option>
                  </Select>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Button
                    variant="contained"
                    onClick={loadScanResults}
                    fullWidth
                  >
                    Apply Filters
                  </Button>
                </Grid>
              </Grid>
            </Box>
          )}
        </Card>

        {/* Alerts */}
        {error && (
          <Alert severity="error" onClose={() => setError(null)} sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}
        {success && (
          <Alert severity="success" onClose={() => setSuccess(null)} sx={{ mb: 2 }}>
            {success}
          </Alert>
        )}

        {/* Scan Progress Modal */}
        {scanProgress && (
          <Modal open={true} onClose={() => {}}>
            <Card sx={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              width: 400,
              p: 4
            }}>
              <Typography variant="h3" sx={{ mb: 2 }}>
                Scanning Pages
              </Typography>
              <Typography variant="body2" sx={{ mb: 2 }}>
                Scanning: {scanProgress.currentPage}
              </Typography>
              <Progress value={scanProgress.percentage} sx={{ mb: 2 }} />
              <Typography variant="body2" align="center">
                {scanProgress.current} of {scanProgress.total} pages
              </Typography>
            </Card>
          </Modal>
        )}

        {/* Results Tabs */}
        <Card>
          <Tabs value={activeTab} onChange={(e, value) => setActiveTab(value)}>
            <Tab label={`Scan Results (${sortedResults.length})`} />
            <Tab label="Pages Overview" />
          </Tabs>

          {/* Scan Results Tab */}
          {activeTab === 0 && (
            <Box sx={{ p: 2 }}>
              {loading ? (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Progress indeterminate />
                  <Typography variant="body2" sx={{ mt: 2 }}>
                    Loading results...
                  </Typography>
                </Box>
              ) : sortedResults.length === 0 ? (
                <Box sx={{ textAlign: 'center', py: 8 }}>
                  <Typography variant="h3" color="text.secondary" sx={{ mb: 2 }}>
                    No scan results found
                  </Typography>
                  <Typography variant="body1" color="text.secondary">
                    Select a page and click "Scan Page" to start checking for accessibility issues.
                  </Typography>
                </Box>
              ) : (
                <Stack spacing={2}>
                  {sortedResults.map((result) => (
                    <Card
                      key={result.scanId}
                      variant="outlined"
                      sx={{
                        p: 2,
                        cursor: 'pointer',
                        '&:hover': { bgcolor: 'action.hover' }
                      }}
                      onClick={() => setSelectedResult(result)}
                    >
                      <Grid container alignItems="center" spacing={2}>
                        <Grid item xs={12} md={4}>
                          <Typography variant="subtitle1" fontWeight="medium">
                            {result.pageTitle}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            {result.pagePath}
                          </Typography>
                        </Grid>
                        <Grid item xs={6} md={2}>
                          <Box sx={{ textAlign: 'center' }}>
                            <Typography variant="h3" color={getScoreColor(result.score)}>
                              {result.score?.toFixed(0)}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              Score
                            </Typography>
                          </Box>
                        </Grid>
                        <Grid item xs={6} md={2}>
                          <Box sx={{ textAlign: 'center' }}>
                            <Typography variant="h4">
                              {result.violationCount}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              Issues
                            </Typography>
                          </Box>
                        </Grid>
                        <Grid item xs={12} md={3}>
                          <Stack direction="row" spacing={1}>
                            {result.criticalCount > 0 && (
                              <Chip
                                label={`${result.criticalCount} Critical`}
                                color="error"
                                size="small"
                              />
                            )}
                            {result.seriousCount > 0 && (
                              <Chip
                                label={`${result.seriousCount} Serious`}
                                color="warning"
                                size="small"
                              />
                            )}
                            {result.moderateCount > 0 && (
                              <Chip
                                label={`${result.moderateCount} Moderate`}
                                color="info"
                                size="small"
                              />
                            )}
                          </Stack>
                        </Grid>
                        <Grid item xs={12} md={1}>
                          <Typography variant="body2" color="text.secondary" align="right">
                            {formatDate(result.scanDate)}
                          </Typography>
                        </Grid>
                      </Grid>
                    </Card>
                  ))}
                </Stack>
              )}
            </Box>
          )}

          {/* Pages Overview Tab */}
          {activeTab === 1 && (
            <Box sx={{ p: 2 }}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Page</TableCell>
                    <TableCell>Path</TableCell>
                    <TableCell>Last Modified</TableCell>
                    <TableCell>Scan Status</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {pages.map((page) => (
                    <TableRow key={page.path}>
                      <TableCell>{page.title || page.name}</TableCell>
                      <TableCell>
                        <Typography variant="body2" fontFamily="monospace">
                          {page.path}
                        </Typography>
                      </TableCell>
                      <TableCell>{formatDate(page.lastModified)}</TableCell>
                      <TableCell>
                        {page.scanStatus?.scanned ? (
                          <Stack direction="row" spacing={1} alignItems="center">
                            <Badge color={getScoreColor(page.scanStatus.score)}>
                              Score: {page.scanStatus.score?.toFixed(0)}
                            </Badge>
                            {page.scanStatus.violationCount > 0 && (
                              <Typography variant="body2" color="text.secondary">
                                ({page.scanStatus.violationCount} issues)
                              </Typography>
                            )}
                          </Stack>
                        ) : (
                          <Typography variant="body2" color="text.secondary">
                            Not scanned
                          </Typography>
                        )}
                      </TableCell>
                      <TableCell>
                        <Button
                          size="small"
                          onClick={() => scanPage(page.path)}
                          disabled={scanning}
                        >
                          Scan
                        </Button>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Box>
          )}
        </Card>

        {/* Result Detail Modal */}
        {selectedResult && (
          <Modal open={true} onClose={() => setSelectedResult(null)}>
            <Card sx={{
              position: 'absolute',
              top: '50%',
              left: '50%',
              transform: 'translate(-50%, -50%)',
              width: '80%',
              maxWidth: 800,
              maxHeight: '80vh',
              overflow: 'auto',
              p: 4
            }}>
              <Box sx={{ mb: 3 }}>
                <Typography variant="h2" sx={{ mb: 1 }}>
                  {selectedResult.pageTitle}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {selectedResult.pagePath}
                </Typography>
              </Box>

              <Grid container spacing={3} sx={{ mb: 3 }}>
                <Grid item xs={3}>
                  <Card variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                    <Typography variant="h3" color={getScoreColor(selectedResult.score)}>
                      {selectedResult.score?.toFixed(0)}
                    </Typography>
                    <Typography variant="body2">Score</Typography>
                  </Card>
                </Grid>
                <Grid item xs={3}>
                  <Card variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                    <Typography variant="h3">{selectedResult.violationCount}</Typography>
                    <Typography variant="body2">Violations</Typography>
                  </Card>
                </Grid>
                <Grid item xs={3}>
                  <Card variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                    <Typography variant="h3">{selectedResult.passCount}</Typography>
                    <Typography variant="body2">Passes</Typography>
                  </Card>
                </Grid>
                <Grid item xs={3}>
                  <Card variant="outlined" sx={{ p: 2, textAlign: 'center' }}>
                    <Typography variant="body1">{selectedResult.wcagLevel}</Typography>
                    <Typography variant="body2">WCAG Level</Typography>
                  </Card>
                </Grid>
              </Grid>

              <Button
                variant="outlined"
                onClick={() => setSelectedResult(null)}
                sx={{ mt: 2 }}
              >
                Close
              </Button>
            </Card>
          </Modal>
        )}
      </Container>
    </ThemeProvider>
  );
}

// Mount the app
const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<AccessibilityCheckerApp />);