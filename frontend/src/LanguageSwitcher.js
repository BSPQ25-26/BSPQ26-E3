import { useI18n } from "./i18n/I18nContext";

const languages = ["en", "es", "fr"];

export default function LanguageSwitcher() {
  const { language, setLanguage, t } = useI18n();

  return (
    <div className="language-switcher" aria-label={t("language.label")}>
      {languages.map((option) => (
        <button
          key={option}
          type="button"
          className={`language-switcher-button${language === option ? " active" : ""}`}
          onClick={() => setLanguage(option)}
          aria-pressed={language === option}
          title={t(`language.options.${option}`)}
        >
          {option.toUpperCase()}
        </button>
      ))}
    </div>
  );
}
