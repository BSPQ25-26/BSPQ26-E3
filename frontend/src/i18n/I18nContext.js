import { createContext, useContext, useEffect, useMemo, useState } from "react";
import { supportedLanguages, translations } from "./translations";

const STORAGE_KEY = "planthub-language";

const localeByLanguage = {
  en: "en-US",
  es: "es-ES",
  fr: "fr-FR",
};

const categoryKeyMap = {
  all: "all",
  indoor: "indoor",
  outdoor: "outdoor",
  succulent: "succulent",
  flowering: "flowering",
};

const errorMatchers = [
  { match: /^invalid credentials\.?$/i, key: "errors.invalidCredentials" },
  { match: /^could not connect/i, key: "errors.connection" },
  { match: /^enter your email/i, key: "errors.resetEmailMissing" },
  { match: /^could not resend/i, key: "errors.resendFailed" },
  { match: /^could not send reset email/i, key: "errors.resetFailed" },
  { match: /^registration failed/i, key: "errors.registrationFailed" },
  { match: /^failed to fetch items$/i, key: "errors.failedToFetchItems" },
  { match: /^failed to fetch plant details$/i, key: "errors.failedToFetchPlantDetails" },
  { match: /^failed to fetch cart$/i, key: "errors.failedToFetchCart" },
  { match: /^failed to remove item$/i, key: "errors.failedToRemoveItem" },
  { match: /^error removing item from cart$/i, key: "errors.errorRemovingItem" },
  { match: /^failed to add item to cart$/i, key: "errors.errorAddingToCart" },
  { match: /^error adding to cart$/i, key: "errors.errorAddingToCart" },
  { match: /not enough stock/i, key: "errors.notEnoughStock" },
  { match: /not available/i, key: "errors.itemNotAvailable" },
  { match: /quantity must be/i, key: "errors.invalidQuantity" },
  { match: /^card number must/i, key: "errors.cardNumberInvalid" },
  { match: /^card holder name is required$/i, key: "errors.cardHolderRequired" },
  { match: /^expiry date must/i, key: "errors.expiryInvalid" },
  { match: /^cvv must/i, key: "errors.cvvInvalid" },
  { match: /^checkout failed$/i, key: "errors.checkoutFailed" },
  { match: /^please select a valid image file$/i, key: "errors.imageFileInvalid" },
  { match: /^supabase is not configured/i, key: "errors.supabaseMissing" },
  { match: /^title is required$/i, key: "errors.titleRequired" },
  { match: /^description is required$/i, key: "errors.descriptionRequired" },
  { match: /^price must be greater than 0$/i, key: "errors.pricePositive" },
  { match: /^quantity must be greater than 0$/i, key: "errors.quantityPositive" },
  { match: /^failed to create item$/i, key: "errors.failedToCreateItem" },
  { match: /^error creating item$/i, key: "errors.errorCreatingItem" },
  { match: /^failed to fetch purchase history$/i, key: "errors.failedToFetchPurchaseHistory" },
  { match: /^failed to fetch sales history$/i, key: "errors.failedToFetchSalesHistory" },
  { match: /^failed to fetch reviews$/i, key: "errors.failedToFetchReviews" },
  { match: /^failed to create review$/i, key: "errors.failedToCreateReview" },
  { match: /^rating must be between 1 and 5$/i, key: "errors.ratingRange" },
  { match: /^email not confirmed\.?$/i, key: "errors.emailNotConfirmed" },
  { match: /^username already taken/i, key: "errors.usernameTaken" },
  { match: /^password reset email sent\.?$/i, key: "errors.passwordResetSent" },
  { match: /^password changed successfully\.?$/i, key: "errors.passwordChanged" },
  { match: /^current password is incorrect\.?$/i, key: "errors.currentPasswordIncorrect" },
  { match: /^confirmation email resent\.?$/i, key: "errors.confirmationResent" },
  { match: /^cannot checkout with empty cart$/i, key: "errors.emptyCartCheckout" },
  { match: /^payment failed or invalid payment details$/i, key: "errors.paymentInvalid" },
  { match: /^profile not found/i, key: "errors.profileNotFound" },
  { match: /^item not found/i, key: "errors.itemNotFound" },
  { match: /^author not found/i, key: "errors.authorNotFound" },
];

function getInitialLanguage() {
  if (typeof window === "undefined") {
    return "en";
  }

  const savedLanguage = window.localStorage.getItem(STORAGE_KEY);
  if (supportedLanguages.includes(savedLanguage)) {
    return savedLanguage;
  }

  const browserLanguage = window.navigator.language?.slice(0, 2).toLowerCase();
  return supportedLanguages.includes(browserLanguage) ? browserLanguage : "en";
}

function getNestedValue(source, key) {
  return key.split(".").reduce((current, part) => current?.[part], source);
}

function interpolate(template, values = {}) {
  if (typeof template !== "string") {
    return template;
  }

  return template.replace(/\{(\w+)\}/g, (_, variable) => {
    const value = values[variable];
    return value === undefined || value === null ? "" : String(value);
  });
}

function translateKey(language, key, values) {
  const languageTable = translations[language] ?? translations.en;
  const template = getNestedValue(languageTable, key) ?? getNestedValue(translations.en, key) ?? key;
  return interpolate(template, values);
}

function createI18nApi(language, setLanguage = () => {}) {
  const locale = localeByLanguage[language] ?? localeByLanguage.en;

  return {
    language,
    locale,
    setLanguage,
    t: (key, values) => translateKey(language, key, values),
    formatCurrency(value) {
      const numericValue = Number(value ?? 0);
      return new Intl.NumberFormat(locale, {
        style: "currency",
        currency: "USD",
      }).format(Number.isFinite(numericValue) ? numericValue : 0);
    },
    formatDate(value, options = {}) {
      if (!value) {
        return translateKey(language, "common.notAvailable");
      }

      return new Intl.DateTimeFormat(locale, options).format(new Date(value));
    },
    translateCategory(value) {
      const key = categoryKeyMap[String(value ?? "").toLowerCase()];
      return key ? translateKey(language, `common.categories.${key}`) : value;
    },
    translateOrderStatus(value) {
      return translateKey(language, `common.orderStatus.${value}`) !== `common.orderStatus.${value}`
        ? translateKey(language, `common.orderStatus.${value}`)
        : value;
    },
    translateItemStatus(value) {
      return value ? translateKey(language, "common.itemStatus.active") : translateKey(language, "common.itemStatus.inactive");
    },
    translateError(message, fallbackKey = "errors.generic") {
      if (!message) {
        return translateKey(language, fallbackKey);
      }

      const matcher = errorMatchers.find(({ match }) => match.test(message));
      if (matcher) {
        return translateKey(language, matcher.key);
      }

      return message;
    },
  };
}

const defaultValue = createI18nApi("en");

const I18nContext = createContext(defaultValue);

export function I18nProvider({ children }) {
  const [language, setLanguage] = useState(getInitialLanguage);

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, language);
    document.documentElement.lang = language;
  }, [language]);

  const value = useMemo(() => createI18nApi(language, setLanguage), [language]);

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>;
}

export function useI18n() {
  return useContext(I18nContext);
}
