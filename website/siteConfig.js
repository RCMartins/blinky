const repoUrl = "https://github.com/rcmartins/blinky";

const siteConfig = {
  title: 'Blinky', // Title for your website.
  tagline: 'Semantic mutation testing for Scala',
  url: 'https://rcmartins.github.io', // Your website URL
  baseUrl: '/blinky/', // Base URL for your project

  customDocsPath: "blinky-docs/target/docs",

  // Used for publishing and more
  projectName: 'blinky',
  organizationName: 'rcmartins',

  // For no header links in the top nav bar -> headerLinks: [],
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

  // Open Graph and Twitter card images.
  //ogImage: 'img/undraw_online.svg',
  //twitterImage: 'img/undraw_tweetstorm.svg',

  // Show documentation's last contributor's name.
  // enableUpdateBy: true,

  // Show documentation's last update time.
  enableUpdateTime: true,

  // You may provide arbitrary config keys to be used as needed by your
  // template. For example, if you need your repo's URL...
  //   repoUrl: 'https://github.com/facebook/test-site',
};

module.exports = siteConfig;
