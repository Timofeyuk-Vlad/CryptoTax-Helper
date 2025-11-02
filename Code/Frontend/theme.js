import { createTheme } from "@mui/material/styles";

const theme = createTheme({
    palette: {
        mode: "light",
        primary: { main: "#000000" },
        secondary: { main: "#000000" },
        text: {
            primary: "#000000",
            secondary: "#222222",
        },
        background: {
            default: "#ffffff",
            paper: "#ffffff",
        },
    },
    shape: {
        borderRadius: 4,
    },
    typography: {
        fontFamily: "'Roboto', 'Segoe UI', sans-serif",
        fontWeightRegular: 400,
        button: {
            textTransform: "none",
            fontWeight: "bold",
        },
    },
    components: {
        MuiPaper: {
            styleOverrides: {
                root: {
                    border: "2px solid #000",
                    boxShadow: "none",
                },
            },
        },
        MuiCard: {
            styleOverrides: {
                root: {
                    border: "2px solid #000",
                    boxShadow: "none",
                },
            },
        },
        MuiButton: {
            styleOverrides: {
                root: {
                    border: "2px solid #000",
                    backgroundColor: "#fff",
                    color: "#000",
                    boxShadow: "none",
                    "&:hover": {
                        backgroundColor: "#000",
                        color: "#fff",
                        boxShadow: "none",
                    },
                },
            },
        },
        MuiTextField: {
            styleOverrides: {
                root: {
                    "& .MuiOutlinedInput-root": {
                        "& fieldset": {
                            border: "2px solid #000",
                        },
                        "&:hover fieldset": {
                            borderColor: "#000",
                        },
                        "&.Mui-focused fieldset": {
                            borderColor: "#000",
                        },
                    },
                    "& .MuiInputLabel-root": {
                        color: "#000",
                    },
                },
            },
        },
        MuiAppBar: {
            styleOverrides: {
                root: {
                    backgroundColor: "#fff",
                    color: "#000",
                    borderBottom: "2px solid #000",
                    boxShadow: "none",
                },
            },
        },
        MuiDivider: {
            styleOverrides: {
                root: {
                    borderColor: "#000",
                    borderWidth: "1px",
                },
            },
        },
    },
});

export default theme;
