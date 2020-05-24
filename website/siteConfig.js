const repoUrl = "https://github.com/rcmartins/blinky";

const siteConfig = {
  title: 'Blinky',
  tagline: 'Semantic mutation testing for Scala',
  url: 'https://rcmartins.github.io',
  baseUrl: '/blinky/',

  customDocsPath: "blinky-docs/target/docs",

  // Used for publishing and more
  projectName: 'blinky',
  organizationName: 'rcmartins',

  headerLinks: [
    { doc: "installation", label: "Docs" },
    { href: repoUrl, label: "GitHub", external: true }
  ],

  /* Colors for website */
  colors: {
    primaryColor: '#0779e4',
    secondaryColor: '#eff3c6',
  },

  // This copyright info is used in /core/Footer.js and blog RSS/Atom feeds.
  copyright: `Copyright © ${new Date().getFullYear()} Blinky`,

  highlight: {
    // Highlight.js theme to use for syntax highlighting in code blocks.
    theme: 'github',
  },

  // Add custom scripts here that would be placed in <script> tags.
  scripts: ['https://buttons.github.io/buttons.js'],

  // On page navigation for the current documentation page.
  onPageNav: 'separate',
  // No .html extensions for paths.
  cleanUrl: true,

  // Show documentation's last update time.
  enableUpdateTime: true,
};

module.exports = siteConfig;
